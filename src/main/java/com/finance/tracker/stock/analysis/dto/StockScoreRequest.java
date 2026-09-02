package com.finance.tracker.stock.analysis.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockScoreRequest(
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal fundamentalScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal valuationScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal growthScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal financialHealthScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal technicalScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal sentimentScore,
        @NotNull @DecimalMin(value = "0.0") @DecimalMax("100.0") BigDecimal riskScore

        ) {
}
