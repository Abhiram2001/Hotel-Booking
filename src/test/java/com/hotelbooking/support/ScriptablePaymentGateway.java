package com.hotelbooking.support;

import com.hotelbooking.payment.domain.gateway.ChargeRequest;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import com.hotelbooking.payment.domain.gateway.RefundRequest;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test gateway: declines the same instruments as the mock gateway, counts calls, and lets a test run an
 * action while a charge is in flight (to simulate a slow gateway or a cancellation racing the payment).
 */
public final class ScriptablePaymentGateway implements PaymentGateway {

    private final Set<String> declinedInstruments;
    private final AtomicInteger charges = new AtomicInteger();
    private final AtomicInteger refunds = new AtomicInteger();
    private final AtomicReference<Runnable> duringNextCharge = new AtomicReference<>();
    private volatile boolean declineRefunds;

    public ScriptablePaymentGateway(Set<String> declinedInstruments) {
        this.declinedInstruments = Set.copyOf(declinedInstruments);
    }

    @Override
    public GatewayResult charge(ChargeRequest request) {
        charges.incrementAndGet();
        Runnable action = duringNextCharge.getAndSet(null);
        if (action != null) {
            action.run();
        }
        return declinedInstruments.contains(request.instrumentToken())
                ? GatewayResult.failure("Declined by issuer")
                : GatewayResult.success("TEST-CHG-" + UUID.randomUUID());
    }

    @Override
    public GatewayResult refund(RefundRequest request) {
        refunds.incrementAndGet();
        return declineRefunds
                ? GatewayResult.failure("Refund rejected by gateway")
                : GatewayResult.success("TEST-RFD-" + UUID.randomUUID());
    }

    public void duringNextCharge(Runnable action) {
        duringNextCharge.set(action);
    }

    public void declineRefunds() {
        declineRefunds = true;
    }

    public int chargeCount() {
        return charges.get();
    }

    public int refundCount() {
        return refunds.get();
    }

    public void reset() {
        charges.set(0);
        refunds.set(0);
        duringNextCharge.set(null);
        declineRefunds = false;
    }
}
