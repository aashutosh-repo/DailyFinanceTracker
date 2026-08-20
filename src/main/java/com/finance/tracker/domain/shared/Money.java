package com.finance.tracker.domain.shared;

import lombok.*;

import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Money implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCALE = 2;
    private final BigDecimal amount;
    private final String currency;

    public static Money of(String amount, String currency) {
        return of(new BigDecimal(amount), currency);
    }

    public static Money of(BigDecimal amount, String currency) {
       if (amount == null) {
           throw new IllegalArgumentException("Amount cannot be Null");
       }
       if (currency == null || currency.isBlank()) {
           throw new IllegalArgumentException("Currency cannot be Null");
       }

       if(amount.signum() < 0) {
           throw new IllegalArgumentException("Amount cannot be Negative");
       }
       BigDecimal normalized = amount.setScale(SCALE, RoundingMode.HALF_UP);
       return new Money(normalized, currency.toUpperCase().trim());
    }

    public static Money zero(String currency) {
        return of(BigDecimal.ZERO, currency);
    }

    public Money add(Money money) {
        if(!this.currency.equals(money.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot add money in different currencies: %s - %s", this.currency, money.currency)
            );
        }
        BigDecimal result = this.amount.subtract(money.amount);
        if(result.signum() < 0) {
            throw  new IllegalArgumentException(
                    String.format("Substring would result in negative amount: %s - %s = %s", this.amount, money.amount, result)
            );
        }
        return of(result, this.currency);
    }

    public boolean isGreaterThan(Money money) {
        ensureSameCurrency(money);
        return this.amount.compareTo(money.amount) > 0;
    }
    public boolean isLessThan(Money money) {
        ensureSameCurrency(money);
        return this.amount.compareTo(money.amount) < 0;
    }
    public boolean isEqual(Money money) {
        ensureSameCurrency(money);
        return this.amount.compareTo(money.amount) == 0;
    }

    public boolean isZero(Money money) {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
    public boolean isPositive() {
        assert this.amount != null;
        return this.amount.signum() > 0;
    }

    private void ensureSameCurrency(Money money) {
        if (money == null || !Objects.equals(this.currency, money.currency)) {
            throw new IllegalArgumentException(
                  String.format("Cannot compare money in different currencies: %s vs %s", this.currency, money !=null ? money.currency : "null")
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }
}
