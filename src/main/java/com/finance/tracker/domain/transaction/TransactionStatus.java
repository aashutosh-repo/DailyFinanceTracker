package com.finance.tracker.domain.transaction;

import lombok.Getter;

@Getter
public enum TransactionStatus {
    PENDING("Pending"),
    POSTED("ARCHIVED"),
    CANCELLED("Cancelled"),
    ARCHIEVED("Archived");

    private final String label;

    TransactionStatus(String label) {
        this.label = label;
    }

    public boolean isActive() {
        return this== POSTED;
    }

    public boolean isMutable() {
        return this == PENDING || this == POSTED;
    }

    public boolean isTerminal() {
        return this== CANCELLED || this == ARCHIEVED;
    }




}
