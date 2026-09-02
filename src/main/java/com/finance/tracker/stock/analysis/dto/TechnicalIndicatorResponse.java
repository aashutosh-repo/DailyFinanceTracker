package com.finance.tracker.stock.analysis.dto;

import com.finance.tracker.domain.analysis.Signal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TechnicalIndicatorResponse(
        String indicator,
        BigDecimal value,
        Signal signal,
        LocalDateTime calculatedAt,
        String source
) {
}
