package com.finance.tracker.stock.market.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MarketData(

        LocalDate priceDate,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume,
        String source,
        LocalDateTime retrievedAt,
        String currency,
        String exchange,
        List<String> dataQualityWarnings

) {
        public MarketData(
                LocalDate priceDate,
                BigDecimal open,
                BigDecimal high,
                BigDecimal low,
                BigDecimal close,
                Long volume,
                String source
        ) {
                this(priceDate, open, high, low, close, volume, source, null, null, null, List.of());
        }
}