package com.finance.tracker.stock.analysis.dto;

import java.math.BigDecimal;
import java.util.List;

public record DcfValuationResponse(
        String symbol,
        BigDecimal enterpriseValue,
        BigDecimal intrinsicValuePerShare,
        List<BigDecimal> projectedFreeCashFlow
) {
}
