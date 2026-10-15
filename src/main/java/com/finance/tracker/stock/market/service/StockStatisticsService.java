package com.finance.tracker.stock.market.service;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketPriceRepository;
import com.finance.tracker.stock.market.dto.StockStatisticsResponse;
import com.finance.tracker.stock.market.entity.MarketPrice;
import com.finance.tracker.stock.market.provider.MarketDataClassification;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
                validateDateRange(fromDate, toDate);
        // =====================================
        // 1. Get Company
        // =====================================

        Company company = companyRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found: " + symbol));

        // =====================================
        // 2. Get Historical Prices
        // =====================================

        List<MarketPrice> prices = marketPriceRepository.findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
                                company.getId(),
                                fromDate,
                                toDate
                        );


                if (prices == null || prices.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No market price data found");
        }

                validatePriceRows(prices, fromDate, toDate);


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


                BigDecimal priceChangePercentage = null;
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
                priceChangePercentage,
                String.join(", ", prices.stream().map(MarketPrice::getSource).filter(java.util.Objects::nonNull).distinct().toList()),
                MarketDataClassification.fromSources(prices.stream().map(MarketPrice::getSource).toList()).name(),
                first.getPriceDate(),
                last.getPriceDate(),
                List.of("No exchange trading calendar is configured; missing dates cannot be classified as missing sessions.")
        );
    }

        private void validateDateRange(LocalDate fromDate, LocalDate toDate) {
                if (fromDate == null || toDate == null || fromDate.isAfter(toDate)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be on or before to");
                }
        }

        private void validatePriceRows(List<MarketPrice> prices, LocalDate fromDate, LocalDate toDate) {
                LocalDate previousDate = null;
                for (MarketPrice price : prices) {
                        if (price == null || price.getPriceDate() == null
                                        || price.getPriceDate().isBefore(fromDate) || price.getPriceDate().isAfter(toDate)
                                        || price.getClosePrice() == null || price.getHighPrice() == null || price.getLowPrice() == null) {
                                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market data is incomplete or outside the requested range");
                        }
                        if (price.getClosePrice().compareTo(BigDecimal.ZERO) <= 0
                                        || price.getHighPrice().compareTo(BigDecimal.ZERO) <= 0
                                        || price.getLowPrice().compareTo(BigDecimal.ZERO) <= 0
                                        || price.getHighPrice().compareTo(price.getClosePrice()) < 0
                                        || price.getLowPrice().compareTo(price.getClosePrice()) > 0) {
                                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market prices are invalid");
                        }
                        if (previousDate != null && !price.getPriceDate().isAfter(previousDate)) {
                                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Stored market dates are duplicated or not chronological");
                        }
                        previousDate = price.getPriceDate();
                }
        }
}