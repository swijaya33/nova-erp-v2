package com.novaerp.common.currency;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable currency value with explicit ISO 4217 code.
 * Used for all monetary fields to prevent mixing currencies silently.
 */
public class Money implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal amount;
    private final String currencyCode; // ISO 4217: IDR, USD, SGD, etc.

    public Money(BigDecimal amount, String currencyCode) {
        if (amount == null) throw new IllegalArgumentException("amount must not be null");
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode must not be blank");
        }
        this.amount = amount.setScale(2, java.math.RoundingMode.HALF_UP);
        this.currencyCode = currencyCode.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money idr(long rupiah) {
        return new Money(BigDecimal.valueOf(rupiah), "IDR");
    }

    /** Add two Money values — must have same currency */
    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currencyCode);
    }

    /** Subtract two Money values — must have same currency */
    public Money subtract(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currencyCode);
    }

    /** Multiply by a scalar */
    public Money multiply(BigDecimal scalar) {
        return new Money(this.amount.multiply(scalar), this.currencyCode);
    }

    /** Convert to another currency using provided exchange rate */
    public Money convertTo(String targetCurrency, BigDecimal exchangeRate) {
        if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("exchangeRate must be positive");
        }
        return new Money(this.amount.multiply(exchangeRate), targetCurrency);
    }

    public boolean isSameCurrency(Money other) {
        return this.currencyCode.equals(other.currencyCode);
    }

    private void assertSameCurrency(Money other) {
        if (!isSameCurrency(other)) {
            throw new IllegalArgumentException(
                "Cannot operate on different currencies: " + this.currencyCode + " vs " + other.currencyCode);
        }
    }

    public BigDecimal getAmount() { return amount; }
    public String getCurrencyCode() { return currencyCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money that)) return false;
        return amount.compareTo(that.amount) == 0 && currencyCode.equals(that.currencyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currencyCode);
    }

    @Override
    public String toString() {
        return currencyCode + " " + amount.toPlainString();
    }
}
