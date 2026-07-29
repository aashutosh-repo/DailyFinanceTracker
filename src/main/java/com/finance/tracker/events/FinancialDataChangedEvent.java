package com.finance.tracker.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
public class FinancialDataChangedEvent {

    private final String userId;
    private final YearMonth month;

}