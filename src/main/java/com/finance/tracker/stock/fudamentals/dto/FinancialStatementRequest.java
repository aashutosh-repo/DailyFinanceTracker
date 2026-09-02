package com.finance.tracker.stock.fudamentals.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialStatementRequest(
       @NotNull @Min(2000) int fiscalYear,
       @NotNull @Min(1) int fiscalQuarter,
        @NotNull LocalDate reportDate,
        BigDecimal revenue,
        BigDecimal operatingIncome,
        BigDecimal ebit,
        BigDecimal netIncome,
        BigDecimal operatingCashFlow,
        BigDecimal capitalExpenditures,
        BigDecimal freeCashFlow,
        BigDecimal totalAssets,
        BigDecimal totalLiability,
        BigDecimal totalEquity,
        BigDecimal totalDebt,
        BigDecimal cash,
        BigDecimal workingCapital,
        String source
) {
}
