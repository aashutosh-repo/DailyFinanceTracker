package com.finance.tracker.domain.analysis.valuation;

import java.math.BigDecimal;

public record ValuationAssumption(
        String name,
        BigDecimal value,
        String unit,
        AssumptionType type
) {
}
