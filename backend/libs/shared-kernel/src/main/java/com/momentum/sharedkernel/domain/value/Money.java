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

    /**
     * Returns a zero monetary value for the provided currency.
     *
     * @param currency ISO currency, must not be {@code null}
     */
    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    /**
     * Factory that accepts amounts expressed in minor units (cents).
     *
     * @param amountMinor amount in minor units
     * @param currency currency the amount belongs to
     */
    public static Money ofMinor(long amountMinor, Currency currency) {
        return new Money(amountMinor, currency);
    }

    /**
     * Factory that accepts amounts expressed in major units (dollars) and converts them
     * to minor units using the currency precision.
     *
     * @param amount amount in major units
     * @param currency currency the amount belongs to
     */
    public static Money ofMajor(BigDecimal amount, Currency currency) {
        int scale = currency.getDefaultFractionDigits();
        BigDecimal scaled = amount.setScale(scale, RoundingMode.HALF_EVEN);
        long minorUnits = scaled.movePointRight(scale).longValueExact();
        return new Money(minorUnits, currency);
    }

    /**
     * Adds another monetary value with the same currency.
     *
     * @param other value to add
     * @return new {@link Money} instance with summed value
     */
    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amountMinor + other.amountMinor, currency);
    }

    /**
     * Subtracts another monetary value with the same currency.
     *
     * @param other value to subtract
     * @return new {@link Money} for the difference
     */
    public Money subtract(Money other) {
        ensureSameCurrency(other);
        return new Money(this.amountMinor - other.amountMinor, currency);
    }

    /** Creates a copy with the amount negated. */
    public Money negate() {
        return new Money(-amountMinor, currency);
    }

    /** @return {@code true} if the amount is below zero. */
    public boolean isNegative() {
        return amountMinor < 0;
    }

    /** @return {@code true} when the amount is exactly zero. */
    public boolean isZero() {
        return amountMinor == 0;
    }

    /** @return amount expressed in minor currency units. */
    public long toMinorUnits() {
        return amountMinor;
    }

    /** @return amount expressed in major currency units as {@link BigDecimal}. */
    public BigDecimal toMajorUnits() {
        int scale = currency.getDefaultFractionDigits();
        return BigDecimal.valueOf(amountMinor, scale);
    }

    /** @return ISO currency associated with the amount. */
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
