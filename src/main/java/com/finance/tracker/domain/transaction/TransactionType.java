package com.finance.tracker.domain.transaction;

import lombok.Getter;

@Getter
public enum TransactionType {
    INCOME("income"),
    EXPENSE("Expense"),
    TRANSFER("Transfer"),
    INVESTMENT("investment");

    private final String label;

    TransactionType(String  label) {
        this.label = label;
    }

    public boolean affectsBalance() {
        return this == INCOME || this == EXPENSE;
    }

    public boolean increaseBalance() {
        return this == INCOME;
    }

    public boolean decreaseBalance() {
        return this == EXPENSE;
    }
}
