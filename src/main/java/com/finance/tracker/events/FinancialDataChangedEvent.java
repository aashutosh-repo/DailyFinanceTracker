package com.finance.tracker.events;

import lombok.Getter;

import java.time.YearMonth;

@Getter
public class FinancialDataChangedEvent {

    private final String userId;
    private final YearMonth month;
    private final ChangeType changeType;

    public FinancialDataChangedEvent(String userId, YearMonth month, ChangeType changeType) {
        this.userId = userId;
        this.month = month;
        this.changeType = changeType;
    }

    public FinancialDataChangedEvent(String userId, YearMonth month) {
        this(userId, month, ChangeType.ALL);
    }
}