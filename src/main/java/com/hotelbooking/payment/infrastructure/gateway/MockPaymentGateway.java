package com.hotelbooking.payment.infrastructure.gateway;

import com.hotelbooking.payment.domain.gateway.ChargeRequest;
import com.hotelbooking.payment.domain.gateway.GatewayResult;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import com.hotelbooking.payment.domain.gateway.RefundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Stands in for a real provider. Approves everything except the configured test instruments, so both
 * outcomes can be exercised predictably.
 */
@Component
class MockPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGateway.class);

    private final MockGatewayProperties properties;

    MockPaymentGateway(MockGatewayProperties properties) {
        this.properties = properties;
    }

    @Override
    public GatewayResult charge(ChargeRequest request) {
        if (properties.declinedInstruments().contains(request.instrumentToken())) {
            log.debug("Mock gateway declined {}", request);
            return GatewayResult.failure("Declined by issuer");
        }
        log.debug("Mock gateway approved {}", request);
        return GatewayResult.success("MOCK-CHG-" + UUID.randomUUID());
    }

    @Override
    public GatewayResult refund(RefundRequest request) {
        log.debug("Mock gateway refunded {} for charge {}", request.amount(), request.chargeReference());
        return GatewayResult.success("MOCK-RFD-" + UUID.randomUUID());
    }
}
