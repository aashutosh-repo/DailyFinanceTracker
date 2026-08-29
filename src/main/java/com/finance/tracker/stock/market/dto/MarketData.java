package com.finance.tracker.stock.market.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketData(

        LocalDate priceDate,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume,
        String source

) {
}