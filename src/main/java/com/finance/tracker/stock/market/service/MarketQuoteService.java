package com.finance.tracker.stock.market.service;

import com.finance.tracker.stock.company.Company;
import com.finance.tracker.stock.company.CompanyRepository;
import com.finance.tracker.stock.market.MarketQuoteRepository;
import com.finance.tracker.stock.market.dto.MarketQuoteData;
import com.finance.tracker.stock.market.dto.MarketQuoteResponse;
import com.finance.tracker.stock.market.entity.MarketQuote;
import com.finance.tracker.stock.market.provider.MarketDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarketQuoteService {
    private final CompanyRepository companyRepository;
    private final MarketQuoteRepository marketQuoteRepository;
    private final MarketDataProvider marketDataProvider;
    private final MarketSessionService marketSessionService;

    @Transactional
    public MarketQuoteResponse getCurrentQuote(String symbol) {
        Company company = companyRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new RuntimeException("Company not found: " + symbol));
        MarketQuoteData data = marketDataProvider.getCurrentQuote(company.getSymbol());
        validate(data);

        MarketQuote quote = marketQuoteRepository.findByCompanyId(company.getId())
                .orElseGet(() -> MarketQuote.builder().company(company).build());
        boolean changed = quote.getId() == null || !sameQuote(quote, data);
        if (changed) {
            quote.setPrice(data.price());
            quote.setChange(data.change());
            quote.setChangePercent(data.changePercent());
            quote.setOpen(data.open());
            quote.setHigh(data.high());
            quote.setLow(data.low());
            quote.setVolume(data.volume());
            quote.setMarketTime(data.marketTime());
            quote.setSource(data.source());
            quote.setFetchedAt(LocalDateTime.now());
            marketQuoteRepository.save(quote);
        }

        return toResponse(company, quote, data, changed ? quote.getFetchedAt() : LocalDateTime.now());
    }

    private boolean sameQuote(MarketQuote quote, MarketQuoteData data) {
        return quote.getPrice().compareTo(data.price()) == 0
                && equals(quote.getChange(), data.change())
                && equals(quote.getChangePercent(), data.changePercent())
                && equals(quote.getOpen(), data.open())
                && equals(quote.getHigh(), data.high())
                && equals(quote.getLow(), data.low())
                && equals(quote.getVolume(), data.volume())
                && equals(quote.getSource(), data.source());
    }

    private boolean equals(Object first, Object second) {
        return first == null ? second == null : first.equals(second);
    }

    private MarketQuoteResponse toResponse(Company company, MarketQuote quote, MarketQuoteData data, LocalDateTime fetchedAt) {
        LocalDateTime marketTime = quote.getMarketTime() != null ? quote.getMarketTime() : data.marketTime();
        return new MarketQuoteResponse(
                company.getSymbol(), company.getName(), company.getExchange(),
                quote.getPrice(), quote.getChange(), quote.getChangePercent(),
                quote.getOpen(), quote.getHigh(), quote.getLow(), quote.getVolume(),
                marketTime, fetchedAt, quote.getSource(),
                marketSessionService.getStatus(marketTime));
    }

    private void validate(MarketQuoteData data) {
        if (data == null || data.price() == null || data.price().compareTo(BigDecimal.ZERO) <= 0
                || data.open() == null || data.high() == null || data.low() == null
                || data.volume() == null || data.volume() < 0 || data.marketTime() == null
                || data.source() == null || data.source().isBlank()) {
            throw new IllegalArgumentException("Market quote response is invalid");
        }
        if (data.high().compareTo(data.price().max(data.open())) < 0
                || data.low().compareTo(data.price().min(data.open())) > 0) {
            throw new IllegalArgumentException("Market quote high/low values are invalid");
        }
    }
}
