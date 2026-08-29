package com.finance.tracker.stock.market.controller;

import com.finance.tracker.stock.market.dto.StockStatisticsResponse;
import com.finance.tracker.stock.market.service.MarketPriceService;
import com.finance.tracker.stock.market.dto.AsyncMarketSyncResponse;
import com.finance.tracker.stock.market.dto.MarketPriceResponse;
import com.finance.tracker.stock.market.dto.MarketSyncJobResponse;
import com.finance.tracker.stock.market.service.StockStatisticsService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockMarketPriceController {

    private final MarketPriceService marketPriceService;
    private final StockStatisticsService stockStatisticsService;


    @PostMapping("/{symbol}/prices/sync")
    public MarketSyncJobResponse syncHistoricalPrices(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return marketPriceService.startHistoricalSync(symbol, from, to);
    }


    @GetMapping("/{symbol}/prices")
    public List<MarketPriceResponse>
    getHistoricalPrices(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return marketPriceService.getHistoricalPrices(symbol, from, to);
    }

    @PostMapping("/{symbol}/prices/sync/async")
    public ResponseEntity<AsyncMarketSyncResponse> syncPricesAsync(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {

        AsyncMarketSyncResponse response = marketPriceService.startAsyncSync(symbol, from, to);
        return ResponseEntity
                .accepted()
                .body(response);
    }

    @GetMapping("/{symbol}/statistics")
    public StockStatisticsResponse getStatistics(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return stockStatisticsService.getStatistics(symbol, from, to);
    }
}