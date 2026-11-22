package com.hotelbooking.cancellation;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.cancellation.application.CancellationResult;
import com.hotelbooking.cancellation.application.CancellationService;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.BusinessRuleViolationException;
import com.hotelbooking.common.domain.error.InvalidStateTransitionException;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentStatus;
import com.hotelbooking.property.domain.CancellationPolicyType;
import com.hotelbooking.property.domain.Property;
import com.hotelbooking.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The test clock starts at 10:00 and check-in is at 14:00, so check-in N days ahead gives 24N + 4 hours notice. */
class CancellationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    CancellationService cancellations;

    @ParameterizedTest(name = "{0}, check-in in {1} day(s) -> {2}% refund")
    @CsvSource({
            "FLEXIBLE,       1,  100",
            "MODERATE,       10, 100",
            "MODERATE,       3,  50",
            "MODERATE,       1,  0",
            "NON_REFUNDABLE, 10, 0"
    })
    void refundFollowsThePolicyAgreedAtBookingAndRoomsAreReleased(CancellationPolicyType policy, int daysAhead,
                                                                   int expectedPercentage) {
        Property property = onboardProperty(policy, 2);
        Booking booking = book(property, days(daysAhead, daysAhead + 2));
        payByUpi(booking);

        CancellationResult result = cancellations.cancel(booking.id());

        assertThat(result.booking().status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(result.refundDecision().refundPercentage()).isEqualTo(expectedPercentage);
        assertThat(result.refundDecision().refundAmount()).isEqualTo(booking.totalAmount().percentage(expectedPercentage));
        if (expectedPercentage > 0) {
            assertThat(result.refundPayment()).get().extracting(Payment::status).isEqualTo(PaymentStatus.REFUNDED);
        } else {
            assertThat(result.refundPayment()).isEmpty();
        }
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(2);
    }

    @Test
    void cancellingAnUnpaidBookingReleasesRoomsWithoutRefund() {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        Booking booking = book(property, days(5, 7));

        CancellationResult result = cancellations.cancel(booking.id());

        assertThat(result.refundDecision().refundAmount()).isEqualTo(Money.zero(Money.INR));
        assertThat(result.refundPayment()).isEmpty();
        assertThat(gateway.refundCount()).isZero();
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(1);
    }

    @Test
    void cannotCancelAfterCheckInTime() {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        Booking booking = book(property, days(0, 2));
        payByUpi(booking);
        clock.advance(Duration.ofHours(5));

        assertRejected(BusinessRuleViolationException.class, "CANCELLATION_WINDOW_CLOSED",
                () -> cancellations.cancel(booking.id()));
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(availableRooms(property, booking.stay().dates())).isZero();
    }

    @Test
    void bookingCanOnlyBeCancelledOnce() {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        Booking booking = book(property, days(5, 7));
        payByUpi(booking);
        cancellations.cancel(booking.id());

        assertThatThrownBy(() -> cancellations.cancel(booking.id())).isInstanceOf(InvalidStateTransitionException.class);
        assertThat(gateway.refundCount()).isEqualTo(1);
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(1);
    }

    @Test
    void failedRefundIsRecordedButDoesNotUndoTheCancellation() {
        Property property = onboardProperty(CancellationPolicyType.FLEXIBLE, 1);
        Booking booking = book(property, days(5, 7));
        payByUpi(booking);
        gateway.declineRefunds();

        CancellationResult result = cancellations.cancel(booking.id());

        assertThat(result.refundPayment()).get().satisfies(payment -> {
            assertThat(payment.status()).isEqualTo(PaymentStatus.REFUND_FAILED);
            assertThat(payment.refundAmount()).isEqualTo(booking.totalAmount());
        });
        assertThat(bookings.get(booking.id()).status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(availableRooms(property, booking.stay().dates())).isEqualTo(1);
    }
}
