package com.finance.tracker.stock.analysis.dto;

import com.finance.tracker.domain.analysis.Signal;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TechnicalAnalysisResponse(
        String symbol,
        LocalDate fromDate,
        LocalDate toDate,
        int recordsUsed,
        List<TechnicalIndicatorResponse> indicators,
        MacdResonse macd,
        BollingerBandsResponse bollingerBands,
        BigDecimal fiftyTwoWeeksHigh,
        BigDecimal fiftyTwoWeeksLow
) {
}
