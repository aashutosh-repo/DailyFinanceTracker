package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.rag.context.MonthlyComparison;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.IncomeService;
import com.finance.tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAnalyticsServiceImpl implements FinancialAnalyticsService {
    private final IncomeService incomeService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    @Override
    @Cacheable(value = "financialContext", key="#userId+ '_' + #month")
    public FinancialContext getMonthlyContext(String userId, YearMonth month) {

        BigDecimal totalIncome = incomeService.getIncomeByMonth(userId, month);

        BigDecimal totalExpense = transactionService.getTotalExpense(userId, month);

        List<CategoryExpense> categoryExpenses =
                Optional.ofNullable(
                                transactionService.getCategoryExpenses(userId, month))
                        .orElse(List.of());

        List<BudgetStatus> budgetStatuses = Optional.ofNullable(
                                budgetService.getMonthlyBudgetsStatus(userId, month))
                                .orElse(List.of());

        BigDecimal totalSavings = totalIncome.subtract(totalExpense);

        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalSavings);
        List<MonthlyComparison> comparisons = buildCategoryComparison(userId, month, categoryExpenses);

        return FinancialContext.builder()
                .userId(userId)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalSavings)
                .categoryExpenses(categoryExpenses)
                .savingsRate(savingsRate)
                .budgetStatuses(budgetStatuses)
                .comparisons(comparisons)
                .build();

        // comparisons for now
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal savings) {

        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return savings.multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);
    }

    private List<MonthlyComparison> buildCategoryComparison(String userId, YearMonth currentMonth, List<CategoryExpense> currentExpense){
        YearMonth previousMonth = currentMonth.minusMonths(1);
        List<CategoryExpense> previousExpense = Optional.ofNullable(
                transactionService.getCategoryExpenses(userId, previousMonth))
                .orElse(List.of());

        Map<String, BigDecimal> previousMap =  previousExpense.stream().collect(
                Collectors.toMap(
                        CategoryExpense::getCategory,
                        CategoryExpense::getAmount
                ));
        return currentExpense.stream()
                .map(e -> new MonthlyComparison(
                        e.getCategory(), previousMap.getOrDefault(e.getCategory(), BigDecimal.ZERO),
                        e.getAmount()
                )).collect(Collectors.toList());
    }
}
