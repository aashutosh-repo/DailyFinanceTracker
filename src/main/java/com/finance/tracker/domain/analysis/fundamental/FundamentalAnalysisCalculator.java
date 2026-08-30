package com.finance.tracker.domain.analysis.fundamental;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FundamentalAnalysisCalculator {
    private static final int SCALE = 4;

    private FundamentalAnalysisCalculator(){}

    public static BigDecimal revenueGrowth(BigDecimal currentRevenue, BigDecimal previousRevenue) {
        return percentageChange(currentRevenue, previousRevenue);
    }

   public static BigDecimal revenueCagr(BigDecimal endingRevenue, BigDecimal beginningRevenue, int year) {
        return cagr(endingRevenue, beginningRevenue, year);
    }

    public static BigDecimal epsGrowth(BigDecimal currentEps, BigDecimal previousEps) {
        return percentageChange(currentEps, previousEps);
    }

    public static BigDecimal epsCagr(BigDecimal endingEps, BigDecimal beginningEps, int year) {
        return cagr(endingEps, beginningEps, year);
    }

    public static BigDecimal freeCashFlowGrowth(BigDecimal currentCashFlowGrowth, BigDecimal previousCashFlowGrowth) {
        return percentageChange(currentCashFlowGrowth, previousCashFlowGrowth);
    }

    public static BigDecimal cashFlowGrowthCagr(BigDecimal endingCashFlowGrowth, BigDecimal beginningCashFlowGrowth, int year) {
        return cagr(endingCashFlowGrowth, beginningCashFlowGrowth, year);
    }

    public static BigDecimal grossMargin(BigDecimal grossProfit, BigDecimal revenue) {
        return percentage(grossProfit, revenue);
    }

    public static BigDecimal operatingMargin(BigDecimal operatingIncome, BigDecimal revenue) {
        return percentage(operatingIncome, revenue);
    }

    public static BigDecimal netMargin(BigDecimal netIncome, BigDecimal revenue) {
        return percentage(netIncome, revenue);
    }

    public static BigDecimal returnOnEquity(BigDecimal netIncome, BigDecimal shareHolderEquity) {
        return percentage(netIncome, shareHolderEquity);
    }

    public static BigDecimal returnOnAssets(BigDecimal netIncome, BigDecimal totalAsset) {
        return percentage(netIncome, totalAsset);
    }

    public static BigDecimal returnOnInvestedCapital(BigDecimal nopat, BigDecimal investedcapital) {
        return percentage(nopat, investedcapital);
    }

    public static BigDecimal debtToEquity(BigDecimal totalDebt, BigDecimal shareHoldersEquity) {
        return percentage(totalDebt, shareHoldersEquity);
    }

    public static BigDecimal netDebt(BigDecimal totalDebt, BigDecimal cashAndEquivalents) {
        return requires(totalDebt).subtract(requires(cashAndEquivalents)).setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal currentRatio(BigDecimal currentAssets, BigDecimal currentLiability) {
        return ratio(currentAssets, currentLiability);
    }

    public static BigDecimal interestCoverage(BigDecimal ebit, BigDecimal interestExpense) {
        return ratio(ebit, interestExpense);
    }

    public static boolean hasEarningsVsFreeCashFlowDivergence(BigDecimal netIncomeGrowth, BigDecimal freeCashFlowGrowth) {
        return requires(netIncomeGrowth).subtract(requires(freeCashFlowGrowth)).abs().compareTo(new BigDecimal("20.000")) > 0;
    }

    public static boolean hasRevenueVsReceivablesDivergence(BigDecimal revenueGrowth, BigDecimal receivableGrowth) {
        return requires(receivableGrowth).subtract(requires(revenueGrowth)).abs().compareTo(new BigDecimal("15.000")) > 0;
    }

    public static boolean hasMarginCompression(BigDecimal currentMargin, BigDecimal previousMargin) {
        return requires(previousMargin).subtract(requires(currentMargin)).abs().compareTo(new BigDecimal("2.000")) > 0;
    }

    public static BigDecimal stockBasedCompensationRatio(BigDecimal stockBasedCompensationRatio, BigDecimal revenue) {
        return percentage(stockBasedCompensationRatio, revenue);
    }

    public static BigDecimal workingCapitalChange(BigDecimal currentWorkingCapital, BigDecimal previousWorkingCapital) {
        return percentage(currentWorkingCapital, previousWorkingCapital);
    }

    private static BigDecimal percentage(BigDecimal numerator, BigDecimal denominator) {
        return ratio(numerator, denominator).multiply(BigDecimal.valueOf(100).setScale(SCALE, RoundingMode.HALF_UP));
    }

    private static BigDecimal ratio(BigDecimal numerator, BigDecimal denominator) {
        return requires(numerator).divide(nonZero(denominator), SCALE, RoundingMode.HALF_UP);
    }


    private static BigDecimal cagr(BigDecimal endingRevenue, BigDecimal beginningRevenue, int year) {
        if (year <= 0 ) {
            throw new IllegalArgumentException("Years must be positive");
        }

        double ratio = requires(endingRevenue).divide(nonZero(beginningRevenue), SCALE+6, RoundingMode.HALF_UP).doubleValue();
        double cagr = Math.pow(ratio, 1.0/year) -1.0;
        return BigDecimal.valueOf(cagr*100).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentageChange(BigDecimal currentRevenue, BigDecimal previousRevenue) {
        return requires(currentRevenue).subtract(requires(previousRevenue))
                .divide(nonZero(previousRevenue), SCALE+2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal nonZero(BigDecimal value) {
        BigDecimal requiresValue = requires(value);
        if (requiresValue.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("dominator must not be zero");
        }
        return requiresValue;
    }

    private static BigDecimal requires(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Financial Value must not be Null");
        }
        return value;
    }

}
