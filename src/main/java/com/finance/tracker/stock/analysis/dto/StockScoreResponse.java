package com.finance.tracker.stock.analysis.dto;

import java.math.BigDecimal;

public record StockScoreResponse(
        String symbol,
        BigDecimal overallScore,
        BigDecimal fundamentalScore,
        BigDecimal growthScore,
        BigDecimal valuationScore,
        BigDecimal financialHealthScore,
        BigDecimal technicalScore,
        BigDecimal sentimentScore,
        BigDecimal riskScore
) {
}
