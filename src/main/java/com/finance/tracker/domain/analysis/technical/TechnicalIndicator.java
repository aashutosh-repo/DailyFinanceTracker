package com.finance.tracker.domain.analysis.technical;

import com.finance.tracker.domain.analysis.Signal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TechnicalIndicator(
        String indicator,
        BigDecimal value,
        Signal signal,
        LocalDateTime calculatedAt,
        String source
) {
}
