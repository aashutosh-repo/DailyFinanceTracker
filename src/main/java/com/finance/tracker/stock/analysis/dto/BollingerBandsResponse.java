package com.finance.tracker.stock.analysis.dto;

import java.math.BigDecimal;

public record BollingerBandsResponse(
        BigDecimal lowerBand,
        BigDecimal middleBand,
        BigDecimal upperBand
) {
}
