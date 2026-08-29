package com.finance.tracker.stock.market.provider;

import com.finance.tracker.stock.market.dto.MarketData;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    public String getProviderName() {
        return "MOCK";
    }
}