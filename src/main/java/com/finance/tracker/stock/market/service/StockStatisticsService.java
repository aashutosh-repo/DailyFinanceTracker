package com.finance.tracker.stock.market.service;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.dto.StockStatisticsResponse;
import com.finance.tracker.stock.market.entity.MarketPrice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockStatisticsService {

    private final CompanyRepository companyRepository;
    private final MarketPriceRepository marketPriceRepository;

    public StockStatisticsResponse getStatistics(
            String symbol,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        // =====================================
        // 1. Get Company
        // =====================================

        Company company = companyRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));

        // =====================================
        // 2. Get Historical Prices
        // =====================================

        List<MarketPrice> prices = marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                                company.getId(),
                                fromDate,
                                toDate
                        );


        if (prices.isEmpty()) {
            throw new RuntimeException("No market price data found");
        }


        // =====================================
        // 3. Basic Prices
        // =====================================

        MarketPrice first = prices.getFirst();
        MarketPrice last = prices.getLast();
        BigDecimal startPrice = first.getClosePrice();
        BigDecimal endPrice = last.getClosePrice();

        // =====================================
        // 4. Calculate High / Low
        // =====================================

        BigDecimal highestPrice = prices.stream()
                        .map(MarketPrice::getHighPrice)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        BigDecimal lowestPrice = prices.stream()
                        .map(MarketPrice::getLowPrice)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

        // =====================================
        // 5. Average Close Price
        // =====================================

        BigDecimal totalClosePrice = prices.stream()
                        .map(MarketPrice::getClosePrice)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        BigDecimal averagePrice = totalClosePrice.divide(
                        BigDecimal.valueOf(
                                prices.size()
                        ),
                        2,
                        RoundingMode.HALF_UP
                );

        // =====================================
        // 6. Price Change
        // =====================================

        BigDecimal priceChange = endPrice.subtract(startPrice);


        BigDecimal priceChangePercentage = BigDecimal.ZERO;


        if (startPrice.compareTo(BigDecimal.ZERO) != 0) {

            priceChangePercentage = priceChange.divide(startPrice, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
        }

        // =====================================
        // 7. Return Response
        // =====================================

        return new StockStatisticsResponse(
                company.getSymbol(),
                fromDate,
                toDate,
                prices.size(),
                startPrice,
                endPrice,
                highestPrice,
                lowestPrice,
                averagePrice,
                priceChange,
                priceChangePercentage
        );
    }
}