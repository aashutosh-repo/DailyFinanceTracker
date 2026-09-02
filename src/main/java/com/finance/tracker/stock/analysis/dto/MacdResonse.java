package com.finance.tracker.stock.analysis.dto;

import com.finance.tracker.domain.analysis.Signal;
import java.math.BigDecimal;

public record MacdResonse(
        BigDecimal macdLine,
        BigDecimal signalLine,
        BigDecimal histogram,
        Signal signal
) {
}
