package com.finance.tracker.domain.analysis.fundamental.dto;

import java.math.BigDecimal;

public record FinancialData(
        BigDecimal latestRevenue,
        BigDecimal latestEbit,
        BigDecimal latestNetIncome,
        BigDecimal latestOperatingCashFlow,
        BigDecimal latestCapEx,
        BigDecimal latestTotalDebt,
        BigDecimal latestTotalEquity,
        BigDecimal latestCash,
        BigDecimal revenueGrowthRate,
        BigDecimal netIncomeGrowthRate,
        BigDecimal operatingCashFlowGrowthRate,
        int yearOfData
) {
}
