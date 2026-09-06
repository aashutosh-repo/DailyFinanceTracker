package com.finance.tracker.stock.market.provider;

import com.finance.tracker.stock.market.dto.MarketData;
import com.finance.tracker.stock.market.dto.MarketQuoteData;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Primary
public class MockMarketDataProvider
        implements MarketDataProvider {

    @Override
    public List<MarketData> getHistoricalPrices(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<MarketData> prices = new ArrayList<>();

        Random random = new Random();

        BigDecimal currentPrice = BigDecimal.valueOf(3500 + random.nextInt(500));

        LocalDate currentDate = fromDate;

        while (!currentDate.isAfter(toDate)) {

            double change = (random.nextDouble() - 0.5) * 100;

            BigDecimal open = currentPrice;
            BigDecimal close = open.add(BigDecimal.valueOf(change));

            BigDecimal high = open.max(close).add(BigDecimal.valueOf(random.nextDouble() * 50));

            BigDecimal low = open.min(close).subtract(BigDecimal.valueOf(random.nextDouble() * 50));

            Long volume = 1_000_000L + random.nextInt(500_000);

            prices.add(new MarketData(
                            currentDate,
                            open,
                            high,
                            low,
                            close,
                            volume,
                            getProviderName()
                    )
            );

            currentPrice = close;
            currentDate = currentDate.plusDays(1);
        }

        return prices;
    }

        @Override
        public MarketQuoteData getCurrentQuote(String symbol) {
        int seed = Math.floorMod(symbol.toUpperCase().hashCode(), 500);
        BigDecimal price = BigDecimal.valueOf(3000L + seed).setScale(2);
        BigDecimal change = BigDecimal.valueOf((seed % 41) - 20).movePointLeft(1).setScale(2);
        BigDecimal previousClose = price.subtract(change);
        BigDecimal changePercent = change
            .divide(previousClose, 4, java.math.RoundingMode.HALF_UP)
            .movePointRight(2)
            .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal open = previousClose.add(BigDecimal.valueOf(4)).setScale(2);
        BigDecimal high = price.max(open).add(BigDecimal.valueOf(12)).setScale(2);
        BigDecimal low = price.min(open).subtract(BigDecimal.valueOf(12)).setScale(2);

        return new MarketQuoteData(
            price, change, changePercent, open, high, low,
            1_000_000L + seed * 1_000L, LocalDateTime.now(), getProviderName());
        }


    @Override
    public String getProviderName() {
        return "MOCK";
    }
}