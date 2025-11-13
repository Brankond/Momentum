package com.momentum.sharedkernel.domain.value;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Money is represented as minor currency units (cents) to avoid floating point errors.
 */
public final class Money implements Comparable<Money>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final long amountMinor;
    private final Currency currency;

    private Money(long amountMinor, Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
        this.amountMinor = amountMinor;
        this.currency = currency;
    }

    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    public static Money ofMinor(long amountMinor, Currency currency) {
        return new Money(amountMinor, currency);
    }

    public static Money ofMajor(BigDecimal amount, Currency currency) {
        int scale = currency.getDefaultFractionDigits();
        BigDecimal scaled = amount.setScale(scale, RoundingMode.HALF_EVEN);
        long minorUnits = scaled.movePointRight(scale).longValueExact();
        return new Money(minorUnits, currency);
    }

    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amountMinor + other.amountMinor, currency);
    }

    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amountMinor - other.amountMinor, currency);
    }

    public Money negate() {
        return new Money(-amountMinor, currency);
    }

    public boolean isNegative() {
        return amountMinor < 0;
    }

    public boolean isZero() {
        return amountMinor == 0;
    }

    public long toMinorUnits() {
        return amountMinor;
    }

    public BigDecimal toMajorUnits() {
        int scale = currency.getDefaultFractionDigits();
        return BigDecimal.valueOf(amountMinor, scale);
    }

    public Currency currency() {
        return currency;
    }

    private void ensureSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: %s vs %s".formatted(currency, other.currency));
        }
    }

    @Override
    public int compareTo(Money other) {
        ensureSameCurrency(other);
        return Long.compare(this.amountMinor, other.amountMinor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money money)) {
            return false;
        }
        return amountMinor == money.amountMinor && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amountMinor, currency);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(currency.getCurrencyCode(), toMajorUnits());
    }
}
