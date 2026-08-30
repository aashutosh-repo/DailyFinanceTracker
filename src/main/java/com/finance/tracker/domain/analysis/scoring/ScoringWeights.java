package com.finance.tracker.domain.analysis.scoring;

import java.math.BigDecimal;

public record ScoringWeights(
        BigDecimal fundamentals,
        BigDecimal valuation,
        BigDecimal growth,
        BigDecimal financialHealth,
        BigDecimal technical,
        BigDecimal sentiments
) {
    public static ScoringWeights defaultWeights() {
        return new ScoringWeights(
        new BigDecimal("0.30"),
        new BigDecimal("0.20"),
        new BigDecimal("0.15"),
        new BigDecimal("0.15"),
        new BigDecimal("0.10"),
        new BigDecimal("0.10")
        );
    }
}
