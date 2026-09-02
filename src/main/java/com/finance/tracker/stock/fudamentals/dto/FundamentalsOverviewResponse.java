package com.finance.tracker.stock.fudamentals.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FundamentalsOverviewResponse(
        String symbol,
        LocalDate asOfDate,
        BigDecimal latestRevenue,
        BigDecimal revenueGrowthRateYoY,
        BigDecimal revenueCAGR,
        BigDecimal latestEbit,
        BigDecimal ebitMargin,
        BigDecimal latestNetIncome,
        BigDecimal netMargin,
        BigDecimal latestOperatingCashFlow,
        BigDecimal freeCashFlow,
        BigDecimal fcfMargin,
        BigDecimal totalDebt,
        BigDecimal totalEquity,
        BigDecimal debtToEquityRatio,
        BigDecimal currentRatio,
        BigDecimal roe,
        BigDecimal roa,
        int yearOfData,
        List<FinancialStatementResponse> historicalStatement
) {
}
