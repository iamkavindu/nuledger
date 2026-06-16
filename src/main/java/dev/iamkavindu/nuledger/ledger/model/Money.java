package dev.iamkavindu.nuledger.ledger.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(long amountMinor, String currency) {

    public Money {
        Objects.requireNonNull(currency, "currency must not be null");
        if (amountMinor < 0) {
            throw new IllegalArgumentException("amountMinor must not be negative");
        }
        if (currency.length() != 3) {
            throw new IllegalArgumentException("currency must be ISO 4217 code");
        }
        Currency.getInstance(currency);
    }

    public static Money ofMinor(long amountMinor, String currency) {
        return new Money(amountMinor, currency.toUpperCase());
    }

    public BigDecimal toDecimal() {
        int fractionDigits = Currency.getInstance(currency).getDefaultFractionDigits();
        if (fractionDigits < 0) {
            fractionDigits = 2;
        }
        return BigDecimal.valueOf(amountMinor, fractionDigits).setScale(4, RoundingMode.UNNECESSARY);
    }

    public static Money fromDecimal(BigDecimal amount, String currency) {
        int fractionDigits = Currency.getInstance(currency).getDefaultFractionDigits();
        if (fractionDigits < 0) {
            fractionDigits = 2;
        }
        long minor = amount.movePointRight(fractionDigits)
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();
        return new Money(minor, currency);
    }
}
