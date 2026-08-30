package com.finance.tracker.domain.analysis.valuation;

import java.math.BigDecimal;

public record DcfInput (
        BigDecimal startingRevenue,
        BigDecimal revenueGrowthRate,
        BigDecimal ebitMargin,
        BigDecimal taxRate,
        BigDecimal wacc,
        BigDecimal capexPercentOfRevenue,
        BigDecimal workingCapitalPercentOfRevenue,
        BigDecimal terminalGrowthRate,
        BigDecimal sharesOutstanding,
        int projectionYears
) {
}
