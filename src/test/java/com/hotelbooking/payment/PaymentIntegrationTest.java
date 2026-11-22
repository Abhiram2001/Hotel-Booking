package com.hotelbooking.payment;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.cancellation.application.CancellationService;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.ConflictException;
import com.hotelbooking.common.domain.error.DomainException;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.payment.application.PayForBookingCommand;
import com.hotelbooking.payment.application.PaymentResult;
import com.hotelbooking.payment.domain.CardInstrument;
import com.hotelbooking.payment.domain.IdempotencyKey;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentStatus;
import com.hotelbooking.payment.domain.UpiInstrument;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.support.Concurrently;
import com.hotelbooking.support.Concurrently.Outcome;
import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentIntegrationTest extends IntegrationTestSupport {

    /** Declined by the gateway: listed in hotel.payment.mock.declined-instruments. */
    private static final CardInstrument DECLINED_CARD =
            new CardInstrument("4000000000000002", "Asha Rao", YearMonth.of(2031, 12));
    private static final UpiInstrument UPI = new UpiInstrument("asha@okicici");

    @Autowired
    CancellationService cancellations;

    private Property property;
    private Booking booking;

    @BeforeEach
    void bookARoom() {
        property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        booking = book(property, days(10, 12));
    }

    @Test
    void successfulChargeConfirmsTheBookingForTheBookedAmount() {
        PaymentResult result = payByUpi(booking);

        assertThat(result.replayed()).isFalse();
        assertThat(result.payment().status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(result.payment().amount()).isEqualTo(booking.totalAmount());
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void declinedChargeKeepsTheBookingPayableSoTheGuestCanRetry() {
        PaymentResult declined = pay(booking, DECLINED_CARD);

        assertThat(declined.payment().status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(availableRooms(property, booking.stay().dates())).isZero();

        PaymentResult retry = payByUpi(booking);

        assertThat(retry.payment().status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(payments.findForBooking(booking.id())).extracting(Payment::status)
                .containsExactly(PaymentStatus.FAILED, PaymentStatus.SUCCEEDED);
    }

    @Test
    void repeatingARequestWithTheSameKeyReturnsTheFirstResultWithoutChargingAgain() {
        PayForBookingCommand command = new PayForBookingCommand(booking.id(), UPI, newKey());

        PaymentResult first = payments.pay(command);
        PaymentResult repeat = payments.pay(command);

        assertThat(repeat.replayed()).isTrue();
        assertThat(repeat.payment().id()).isEqualTo(first.payment().id());
        assertThat(gateway.chargeCount()).isEqualTo(1);
    }

    @Test
    void declinedResultIsReplayedTooSoTheSameKeyNeverChargesTwice() {
        PayForBookingCommand command = new PayForBookingCommand(booking.id(), DECLINED_CARD, newKey());

        payments.pay(command);
        PaymentResult repeat = payments.pay(command);

        assertThat(repeat.replayed()).isTrue();
        assertThat(repeat.payment().status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(gateway.chargeCount()).isEqualTo(1);
    }

    @Test
    void reusingAKeyForADifferentRequestIsRejected() {
        IdempotencyKey key = newKey();
        payments.pay(new PayForBookingCommand(booking.id(), DECLINED_CARD, key));

        assertRejected(BusinessRuleViolationException.class, "IDEMPOTENCY_KEY_REUSED",
                () -> payments.pay(new PayForBookingCommand(booking.id(), UPI, key)));
        assertThat(gateway.chargeCount()).isEqualTo(1);
    }

    @Test
    void simultaneousRequestsWithTheSameKeyChargeExactlyOnce() throws Exception {
        PayForBookingCommand command = new PayForBookingCommand(booking.id(), UPI, newKey());
        gateway.duringNextCharge(() -> sleep(Duration.ofMillis(300)));

        List<Outcome<PaymentResult>> outcomes = Concurrently.run(8, index -> payments.pay(command));

        assertThat(outcomes).filteredOn(outcome -> outcome.succeeded() && !outcome.value().replayed()).hasSize(1);
        assertThat(outcomes).filteredOn(outcome -> !outcome.succeeded())
                .allSatisfy(outcome -> assertThat(code(outcome)).isEqualTo("PAYMENT_IN_PROGRESS"));
        assertThat(gateway.chargeCount()).isEqualTo(1);
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void simultaneousRequestsWithDifferentKeysStillChargeOnlyOnce() throws Exception {
        gateway.duringNextCharge(() -> sleep(Duration.ofMillis(300)));

        List<Outcome<PaymentResult>> outcomes = Concurrently.run(8, index -> payByUpi(booking));

        assertThat(outcomes).filteredOn(Outcome::succeeded).hasSize(1);
        assertThat(outcomes).filteredOn(outcome -> !outcome.succeeded())
                .allSatisfy(outcome -> assertThat(code(outcome)).isIn("PAYMENT_IN_PROGRESS", "BOOKING_NOT_PAYABLE"));
        assertThat(gateway.chargeCount()).isEqualTo(1);
    }

    @Test
    void confirmedBookingCannotBePaidAgain() {
        payByUpi(booking);

        assertRejected(ConflictException.class, "BOOKING_NOT_PAYABLE", () -> payByUpi(booking));
        assertThat(gateway.chargeCount()).isEqualTo(1);
    }

    @Test
    void paymentAfterTheHoldRunsOutIsRejectedWithoutCharging() {
        clock.advance(Duration.ofMinutes(15));

        assertRejected(ConflictException.class, "BOOKING_HOLD_EXPIRED", () -> payByUpi(booking));
        assertThat(gateway.chargeCount()).isZero();
    }

    @Test
    void invalidInstrumentIsRejectedBeforeAnythingIsRecorded() {
        CardInstrument expired = new CardInstrument("4242424242424242", "Asha Rao", YearMonth.of(2030, 5));

        assertRejected(DomainValidationException.class, "CARD_EXPIRED", () -> pay(booking, expired));
        assertThat(payments.findForBooking(booking.id())).isEmpty();
    }

    @Test
    void chargeThatSucceedsAfterTheBookingWasCancelledIsRefundedInFull() {
        gateway.duringNextCharge(() -> cancellations.cancel(booking.id()));

        PaymentResult result = payByUpi(booking);

        assertThat(result.payment().status()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.payment().refundAmount()).isEqualTo(booking.totalAmount());
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(1);
    }

    private static String code(Outcome<?> outcome) {
        return outcome.error() instanceof DomainException domain ? domain.code() : outcome.error().toString();
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
