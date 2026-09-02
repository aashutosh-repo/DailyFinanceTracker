package com.finance.tracker.stock.analysis.controller;


import com.finance.tracker.stock.analysis.dto.*;
import com.finance.tracker.stock.analysis.services.StockAnalysisService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class AnalysisController {
    private final StockAnalysisService stockAnalysisService;

    @PostMapping("/{symbol}/technical")
    public TechnicalAnalysisResponse getTechnicalAnalysis(
            @PathVariable String symbol,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to,
            @RequestParam(defaultValue = "14") @Min(2) @Max(200) int period
            ) {
        return stockAnalysisService.getTechnicalAnalysis(symbol, from, to, period);
    }

    @PostMapping("/{symbol}/valuation/dcf")
    public DcfValuationResponse calculateDcf(@PathVariable String symbol,
    @Valid @RequestBody DcfValuationRequest request) {
        return stockAnalysisService.calculateDcf(symbol,request);
    }

    @PostMapping("/{symbol}/valuation/dcf/from-fundamental")
    public DcfValuationResponse calculateDcfFromFundamentals(
            @PathVariable String symbol,
            @Valid @RequestBody DcfFromFundamentalsRequest request
    ) {
        return stockAnalysisService.calculateDcfFromFundamentals(symbol, request);
    }

    @PostMapping("/{symbol}/score")
    public StockScoreResponse calculateScore(
            @PathVariable String symbol,
            @Valid @RequestBody StockScoreRequest request
            ) {
        return stockAnalysisService.calculateScore(symbol, request);
    }
}
