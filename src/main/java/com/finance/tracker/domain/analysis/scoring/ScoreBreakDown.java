package com.finance.tracker.domain.analysis.scoring;

import java.math.BigDecimal;

public record ScoreBreakDown(
        BigDecimal overallScore,
        BigDecimal fundamentalScore,
        BigDecimal valuationScore,
        BigDecimal growthScore,
        BigDecimal financialHealthScore,
        BigDecimal technicalScore,
        BigDecimal sentimentScore,
        BigDecimal riskScore
) {
}
