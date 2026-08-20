package com.finance.tracker.domain.transaction;

public enum TransactionStatus {
    PENDING("Pending"),
    POSTED("Posted"),
    CANCELLED("Cancelled"),
    ARCHIEVED("Archived");

    private final String label;

    TransactionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this== POSTED;
    }

    public boolean isMutable() {
        return this== PENDING || this == POSTED;
    }

    public boolean isTerminal() {
        return this== CANCELLED || this == ARCHIEVED;
    }




}
