package com.hotelbooking.payment.application.processor;

import com.hotelbooking.booking.domain.BookingId;
import com.hotelbooking.common.domain.Money;
import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.payment.domain.CardInstrument;
import com.hotelbooking.payment.domain.IdempotencyKey;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.UpiInstrument;
import com.hotelbooking.payment.domain.WalletInstrument;
import com.hotelbooking.payment.domain.WalletProvider;
import com.hotelbooking.payment.domain.gateway.ChargeRequest;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import com.hotelbooking.payment.domain.gateway.RefundRequest;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Supplier;

import static com.hotelbooking.support.DomainAssertions.assertRejected;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentProcessorTest {

    private static final Clock JUNE_2030 = Clock.fixed(Instant.parse("2030-06-01T04:30:00Z"), ZoneId.of("Asia/Kolkata"));

    private final RecordingGateway gateway = new RecordingGateway();
    private final PaymentProcessors processors = new PaymentProcessors(List.of(
            new CardPaymentProcessor(gateway, JUNE_2030), new UpiPaymentProcessor(gateway), new WalletPaymentProcessor(gateway)));

    @Test
    void eachMethodHasItsOwnProcessor() {
        for (PaymentMethod method : PaymentMethod.values()) {
            assertThat(processors.forMethod(method).method()).isEqualTo(method);
        }
    }

    @Test
    void chargeSendsOurPaymentIdAndTheBookingAmount() {
        UpiInstrument upi = new UpiInstrument("asha@okicici");
        Payment payment = paymentWith(upi);

        GatewayResult result = processors.forMethod(PaymentMethod.UPI).charge(payment, upi);

        assertThat(result.successful()).isTrue();
        assertThat(gateway.lastCharge.merchantReference()).isEqualTo(payment.id().toString());
        assertThat(gateway.lastCharge.amount()).isEqualTo(Money.inr(8000));
        assertThat(gateway.lastCharge.instrumentToken()).isEqualTo("asha@okicici");
    }

    @Test
    void expiredCardIsRejectedBeforeAnyCharge() {
        CardInstrument expired = new CardInstrument("4242424242424242", "Asha Rao", YearMonth.of(2030, 5));

        assertRejected(DomainValidationException.class, "CARD_EXPIRED",
                () -> processors.forMethod(PaymentMethod.CARD).validate(expired));
        assertThat(gateway.lastCharge).isNull();
    }

    @Test
    void processorRefusesAnInstrumentOfAnotherMethod() {
        WalletInstrument wallet = new WalletInstrument(WalletProvider.PHONEPE, "9812345678");

        assertRejected(DomainValidationException.class, "INSTRUMENT_METHOD_MISMATCH",
                () -> processors.forMethod(PaymentMethod.CARD).validate(wallet));
    }

    @Test
    void gatewayOutageIsReportedAsAFailedCharge() {
        gateway.chargeBehaviour = () -> {
            throw new IllegalStateException("connection reset");
        };
        UpiInstrument upi = new UpiInstrument("asha@okicici");

        GatewayResult result = processors.forMethod(PaymentMethod.UPI).charge(paymentWith(upi), upi);

        assertThat(result.successful()).isFalse();
        assertThat(result.failureReason()).isEqualTo("Payment gateway unavailable");
    }

    private static Payment paymentWith(PaymentInstrument instrument) {
        return Payment.initiate(BookingId.newId(), Money.inr(8000), instrument, new IdempotencyKey("key-00000001"),
                "fingerprint", Instant.now(JUNE_2030));
    }

    private static final class RecordingGateway implements PaymentGateway {

        private ChargeRequest lastCharge;
        private Supplier<GatewayResult> chargeBehaviour = () -> GatewayResult.success("CHG-1");

        @Override
        public GatewayResult charge(ChargeRequest request) {
            lastCharge = request;
            return chargeBehaviour.get();
        }

        @Override
        public GatewayResult refund(RefundRequest request) {
            return GatewayResult.success("RFD-1");
        }
    }
}
