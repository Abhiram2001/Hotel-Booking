package com.hotelbooking.payment.application.processor;

import com.hotelbooking.common.domain.error.DomainValidationException;
import com.hotelbooking.payment.domain.CardInstrument;
import com.hotelbooking.payment.domain.PaymentMethod;
import com.hotelbooking.payment.domain.gateway.PaymentGateway;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.YearMonth;

@Component
class CardPaymentProcessor extends GatewayPaymentProcessor<CardInstrument> {

    private final Clock clock;

    CardPaymentProcessor(PaymentGateway gateway, Clock clock) {
        super(gateway, CardInstrument.class);
        this.clock = clock;
    }

    @Override
    public PaymentMethod method() {
        return PaymentMethod.CARD;
    }

    @Override
    protected void validateInstrument(CardInstrument card) {
        if (card.isExpiredAt(YearMonth.now(clock))) {
            throw new DomainValidationException("CARD_EXPIRED", "Card %s has expired".formatted(card.maskedIdentifier()));
        }
    }

    @Override
    protected String gatewayToken(CardInstrument card) {
        return card.cardNumber();
    }
}
