package com.hotelbooking.common.domain;

import com.hotelbooking.common.domain.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void normalisesToTwoDecimalPlacesSoEqualAmountsAreEqual() {
        assertThat(Money.inr(new BigDecimal("100"))).isEqualTo(Money.inr(new BigDecimal("100.000")));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> Money.inr(-1)).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void percentageRoundsHalfUp() {
        assertThat(Money.inr(new BigDecimal("100.01")).percentage(50)).isEqualTo(Money.inr(new BigDecimal("50.01")));
    }

    @Test
    void arithmeticKeepsCurrency() {
        Money total = Money.inr(1500).times(2).plus(Money.inr(500));

        assertThat(total).isEqualTo(Money.inr(3500));
        assertThat(total.dividedBy(4)).isEqualTo(Money.inr(new BigDecimal("875")));
    }

    @Test
    void refusesToMixCurrencies() {
        Money usd = Money.of(BigDecimal.ONE, Currency.getInstance("USD"));

        assertThatThrownBy(() -> Money.inr(1).plus(usd))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("Cannot combine");
    }
}
