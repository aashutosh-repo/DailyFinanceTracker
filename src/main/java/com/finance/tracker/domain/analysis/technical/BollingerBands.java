package com.finance.tracker.domain.analysis.technical;

import java.math.BigDecimal;

public record BollingerBands(
        BigDecimal lowerBand,
        BigDecimal upperBand,
        BigDecimal middleBand
) {
}
