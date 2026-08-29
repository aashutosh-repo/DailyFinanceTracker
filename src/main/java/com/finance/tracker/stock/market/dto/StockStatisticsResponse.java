package com.finance.tracker.stock.market.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public record StockStatisticsResponse(
        String symbol,
        LocalDate fromDate,
        LocalDate toDate,
        long totalRecords,
        BigDecimal startPrice,
        BigDecimal endPrice,
        BigDecimal highestPrice,
        BigDecimal lowestPrice,
        BigDecimal averagePrice,
        BigDecimal priceChange,
        BigDecimal priceChangePercentage

) {
}