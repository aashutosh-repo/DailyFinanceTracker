package com.finance.tracker.stock.market.provider;

import com.finance.tracker.stock.market.dto.MarketData;
import com.finance.tracker.stock.market.dto.MarketQuoteData;

import java.time.LocalDate;
import java.util.List;

public interface MarketDataProvider {

    List<MarketData> getHistoricalPrices(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate
    );

    default MarketQuoteData getCurrentQuote(String symbol) {
        throw new UnsupportedOperationException("Current quotes are not supported by " + getProviderName());
    }

    String getProviderName();
}