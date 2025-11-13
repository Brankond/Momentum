package com.momentum.sharedkernel.domain.value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void addsMoneyOfSameCurrency() {
        Money left = Money.ofMinor(500, USD);
        Money right = Money.ofMinor(250, USD);

        Money result = left.add(right);

        assertThat(result.toMinorUnits()).isEqualTo(750);
    }

    @Test
    void subtractionRejectsDifferentCurrencies() {
        Money usd = Money.ofMinor(100, USD);
        Money eur = Money.ofMinor(100, Currency.getInstance("EUR"));

        assertThatThrownBy(() -> usd.subtract(eur)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void convertsFromMajorUnitsSafely() {
        Money amount = Money.ofMajor(new BigDecimal("10.25"), USD);

        assertThat(amount.toMinorUnits()).isEqualTo(1025);
        assertThat(amount.toMajorUnits()).isEqualTo(new BigDecimal("10.25"));
    }
}
