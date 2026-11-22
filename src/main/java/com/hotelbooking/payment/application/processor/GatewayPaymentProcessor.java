package com.hotelbooking.payment.application.processor;

import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.payment.domain.Payment;
import com.hotelbooking.payment.domain.PaymentInstrument;
import com.hotelbooking.payment.domain.gateway.ChargeRequest;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import com.hotelbooking.payment.domain.gateway.RefundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Shared flow for processors that talk to a {@link PaymentGateway}: check the instrument type, run the
 * method's own validation, build the gateway request. Subclasses only supply what differs per method.
 */
public abstract class GatewayPaymentProcessor<T extends PaymentInstrument> implements PaymentProcessor {

    private static final Logger log = LoggerFactory.getLogger(GatewayPaymentProcessor.class);

    private final PaymentGateway gateway;
    private final Class<T> instrumentType;

    protected GatewayPaymentProcessor(PaymentGateway gateway, Class<T> instrumentType) {
        this.gateway = gateway;
        this.instrumentType = instrumentType;
    }

    @Override
    public final void validate(PaymentInstrument instrument) {
        validateInstrument(typed(instrument));
    }

    @Override
    public final GatewayResult charge(Payment payment, PaymentInstrument instrument) {
        ChargeRequest request = new ChargeRequest(payment.id().toString(), payment.amount(), method(),
                gatewayToken(typed(instrument)));
        return callGateway("charge", payment, () -> gateway.charge(request));
    }

    @Override
    public GatewayResult refund(Payment payment) {
        RefundRequest request = new RefundRequest(payment.id().toString(), payment.gatewayReference(),
                payment.refundAmount());
        return callGateway("refund", payment, () -> gateway.refund(request));
    }

    /** Hook for method-specific rules, e.g. card expiry. */
    protected void validateInstrument(T instrument) {
    }

    /** The identifier the gateway charges for this instrument. */
    protected abstract String gatewayToken(T instrument);

    private T typed(PaymentInstrument instrument) {
        if (!instrumentType.isInstance(instrument)) {
            throw new DomainValidationException("INSTRUMENT_METHOD_MISMATCH",
                    "%s processor cannot handle %s".formatted(method(), instrument.method()));
        }
        return instrumentType.cast(instrument);
    }

    /**
     * A gateway outage is reported as a failed result so the payment does not stay INITIATED forever. With a
     * real provider the outcome of a timed-out call is unknown and should be reconciled by querying the gateway.
     */
    private GatewayResult callGateway(String operation, Payment payment, Supplier<GatewayResult> call) {
        try {
            return call.get();
        } catch (RuntimeException ex) {
            log.error("Gateway {} failed for payment {}", operation, payment.id(), ex);
            return GatewayResult.failure("Payment gateway unavailable");
        }
    }
}
