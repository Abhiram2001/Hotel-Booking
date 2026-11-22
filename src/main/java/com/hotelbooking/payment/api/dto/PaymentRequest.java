package com.hotelbooking.payment.api.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.hotelbooking.payment.domain.PaymentInstrument;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;

/** The {@code method} field selects the request shape. A new method adds one subtype here. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "method")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CardPaymentRequest.class, name = "CARD"),
        @JsonSubTypes.Type(value = UpiPaymentRequest.class, name = "UPI"),
        @JsonSubTypes.Type(value = WalletPaymentRequest.class, name = "WALLET")
})
@Schema(oneOf = {CardPaymentRequest.class, UpiPaymentRequest.class, WalletPaymentRequest.class},
        discriminatorProperty = "method",
        discriminatorMapping = {
                @DiscriminatorMapping(value = "CARD", schema = CardPaymentRequest.class),
                @DiscriminatorMapping(value = "UPI", schema = UpiPaymentRequest.class),
                @DiscriminatorMapping(value = "WALLET", schema = WalletPaymentRequest.class)
        })
public interface PaymentRequest {

    PaymentInstrument toInstrument();
}
