package com.finance.tracker.stock.market.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MarketQuoteData(
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        Long volume,
        LocalDateTime marketTime,
                String source,
                LocalDateTime retrievedAt,
                String currency,
                String exchange,
                List<String> dataQualityWarnings
) {
        public MarketQuoteData(
                        BigDecimal price,
                        BigDecimal change,
                        BigDecimal changePercent,
                        BigDecimal open,
                        BigDecimal high,
                        BigDecimal low,
                        Long volume,
                        LocalDateTime marketTime,
                        String source
        ) {
                this(price, change, changePercent, open, high, low, volume, marketTime, source, null, null, null, List.of());
        }
}
