package com.finance.tracker.stock.market.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketQuoteResponse(
        String symbol,
        String companyName,
        String exchange,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        Long volume,
        LocalDateTime marketTime,
        LocalDateTime fetchedAt,
        String source,
        String marketStatus
) {
}
