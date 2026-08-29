package com.finance.tracker.stock.market.provider;

import com.finance.tracker.stock.market.dto.MarketData;

import java.time.LocalDate;
import java.util.List;

public interface MarketDataProvider {

    List<MarketData> getHistoricalPrices(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate
    );

    String getProviderName();
}