package com.finance.tracker.stock.market.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class MarketSessionService {
    private static final LocalTime OPEN = LocalTime.of(9, 15);
    private static final LocalTime CLOSE = LocalTime.of(15, 30);

    public String getStatus(LocalDateTime marketTime) {
        DayOfWeek day = marketTime.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean duringSession = !marketTime.toLocalTime().isBefore(OPEN)
                && marketTime.toLocalTime().isBefore(CLOSE);
        return weekday && duringSession ? "OPEN" : "CLOSED";
    }
}
