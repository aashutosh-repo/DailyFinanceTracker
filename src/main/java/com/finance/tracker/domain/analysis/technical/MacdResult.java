package com.finance.tracker.domain.analysis.technical;

import com.finance.tracker.domain.analysis.Signal;

import java.math.BigDecimal;

public record MacdResult(
        BigDecimal macdLine,
        BigDecimal signalLine,
        BigDecimal histogram,
        Signal signal
) {
}
