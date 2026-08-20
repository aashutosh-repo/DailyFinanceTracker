package com.finance.tracker.domain.transaction;

public enum TransactionType {
    INCOME("income"),
    EXPENSE("Expense"),
    TRANSSFER("Transfer"),
    INVESTMENT("investment");

    private final String label;

    TransactionType(String  label) {
        this.label = label;
    }

    public String  getLabel() {
        return label;
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
