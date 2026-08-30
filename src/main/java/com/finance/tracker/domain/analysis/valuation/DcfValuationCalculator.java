package com.finance.tracker.domain.analysis.valuation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DcfValuationCalculator {
    private static final int SCALE = 4;
    private DcfValuationCalculator(){}

    public static DcfValuationResult calculate(DcfInput input) {
        validate(input);
        List<BigDecimal> projectedCashFlows = new ArrayList<>();
        BigDecimal enterprisevalue = BigDecimal.ZERO;
        BigDecimal revenue = input.startingRevenue();

        for (int year =1; year < input.projectionYears(); year++) {
            revenue = revenue.multiply(BigDecimal.ONE.add(input.revenueGrowthRate()));
            BigDecimal ebit = revenue.multiply(input.ebitMargin());
            BigDecimal nopat = ebit.multiply(BigDecimal.ONE.subtract(input.taxRate()));
            BigDecimal capex = revenue.multiply(input.capexPercentOfRevenue());
            BigDecimal workingCapitalInvestment = revenue.multiply(input.workingCapitalPercentOfRevenue());
            BigDecimal freeCashFlow = nopat.subtract(capex).subtract(workingCapitalInvestment).setScale(SCALE, RoundingMode.HALF_UP);
            projectedCashFlows.add(freeCashFlow);
            enterprisevalue = enterprisevalue.add(discount(freeCashFlow, input.wacc(), year));
        }

        BigDecimal finalCashFlow = projectedCashFlows.getLast();
        BigDecimal terminalValue = finalCashFlow.multiply(BigDecimal.ONE.add(input.terminalGrowthRate()))
                .divide(input.wacc().subtract(input.terminalGrowthRate()), SCALE+4, RoundingMode.HALF_UP);

        enterprisevalue = enterprisevalue.add(discount(terminalValue, input.wacc(), input.projectionYears()));
        BigDecimal valuePerShare = enterprisevalue.divide(input.sharesOutstanding(), SCALE, RoundingMode.HALF_UP);
        return new DcfValuationResult(enterprisevalue.setScale(SCALE, RoundingMode.HALF_UP), valuePerShare, projectedCashFlows);
    }

    private static BigDecimal discount(BigDecimal cashFlow, BigDecimal discountRate, int year) {
        BigDecimal divisor = BigDecimal.valueOf(Math.pow(BigDecimal.ONE.add(discountRate).doubleValue(), year));
        return cashFlow.divide(divisor, SCALE, RoundingMode.HALF_UP);
    }

    private static void validate(DcfInput input) {
        if(input == null || input.projectionYears() <= 0) {
            throw new IllegalArgumentException("DCF input must include a positive projection period");
        }
        if(input.wacc().compareTo(input.terminalGrowthRate()) < 0) {
            throw new IllegalArgumentException("WACC must be greater than terminal growth rate");
        }
        if(input.sharesOutstanding().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Shares outstanding must be positive");
        }

    }
}
