package com.finance.tracker.stock.analysis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DcfFromFundamentalsRequest(
        BigDecimal revenueGrowthRateOverride,
        BigDecimal ebitMarginOverride,
        BigDecimal capexPercentOfRevenueOverride,
        BigDecimal workingCapitalPercentOfRevenueOverride,
        @NotNull BigDecimal taxRate,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal wacc,
        @NotNull BigDecimal terminalGrowthRate,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal shareOutstanding,
        @Min(1) @Max(10) int projectionYear
        ) {
}
