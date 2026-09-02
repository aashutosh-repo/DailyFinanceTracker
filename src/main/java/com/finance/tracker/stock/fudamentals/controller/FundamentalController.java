package com.finance.tracker.stock.fudamentals.controller;


import com.finance.tracker.stock.fudamentals.dto.FinancialStatementRequest;
import com.finance.tracker.stock.fudamentals.dto.FinancialStatementResponse;
import com.finance.tracker.stock.fudamentals.dto.FundamentalsOverviewResponse;
import com.finance.tracker.stock.fudamentals.services.FundamentalsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stocks/{symbol}/fundamentals")
@RequiredArgsConstructor
@Validated
public class FundamentalController {

    private final FundamentalsService fundamentalsService;

    @GetMapping
    public ResponseEntity<FundamentalsOverviewResponse> getFundamentalsOverview(@PathVariable String symbol) {
        FundamentalsOverviewResponse response = fundamentalsService.getFundamentalsOverview(symbol);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/statement")
    public ResponseEntity<FinancialStatementResponse> saveFinancialData(@PathVariable String symbol,
                                                                        @Valid @RequestBody FinancialStatementRequest request) {
        FinancialStatementResponse response = fundamentalsService.saveFinancialStatement(symbol, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
