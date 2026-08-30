package com.finance.tracker.domain.analysis.valuation;

import java.math.BigDecimal;
import java.util.List;

public record DcfValuationResult(
        BigDecimal enterpriseValue,
        BigDecimal intrinsicValuePerShare,
        List<BigDecimal> projectedFreeCashFlow
) {
}
