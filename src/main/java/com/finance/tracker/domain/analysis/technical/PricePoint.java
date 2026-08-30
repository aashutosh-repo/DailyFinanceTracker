package com.finance.tracker.domain.analysis.technical;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PricePoint(
        LocalDate date,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume
) {
}
