package com.finance.tracker.domain.analysis.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public final class StockScoringEngine {
    private StockScoringEngine() {}

    public static ScoreBreakDown score(
        BigDecimal fundamentalScore,
        BigDecimal valuationScore,
        BigDecimal growthScore,
        BigDecimal financialHealthScore,
        BigDecimal technicalScore,
        BigDecimal sentimentScore,
        BigDecimal riskScore,
        ScoringWeights weights
        ) {
        ScoringWeights effectiveWeights = weights == null ? ScoringWeights.defaultWeights() : weights;
        validateWeights(effectiveWeights);
        BigDecimal overallScore = weighted(fundamentalScore, effectiveWeights.fundamentals())
                .add(weighted(valuationScore, effectiveWeights.valuation()))
                .add(weighted(growthScore, effectiveWeights.growth()))
                .add(weighted(financialHealthScore, effectiveWeights.financialHealth()))
                .add(weighted(technicalScore, effectiveWeights.technical()))
                .add(weighted(sentimentScore, effectiveWeights.sentiments()))
                .setScale(2, RoundingMode.HALF_UP);


        return new ScoreBreakDown(overallScore, fundamentalScore, valuationScore, growthScore, financialHealthScore, technicalScore, sentimentScore, riskScore);
    }

    private static BigDecimal weighted(BigDecimal score, BigDecimal weight) {
        validationScore(score);
        return score.multiply(weight);
    }

    private static void validationScore(BigDecimal score) {
        if (score == null || score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
    }

    private static void validateWeights(ScoringWeights weights) {
        BigDecimal sum = weights.fundamentals()
                .add(weights.valuation())
                .add(weights.growth())
                .add(weights.financialHealth())
                .add(weights.technical())
                .add(weights.sentiments());
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new IllegalArgumentException("scoring weight must sum to 1.00");
        }
    }
}
