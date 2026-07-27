package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialAnalyticsService;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.IncomeService;
import com.finance.tracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinancialAnalyticsServiceImpl implements FinancialAnalyticsService {
    private final IncomeService incomeService;
    private final TransactionService transactionService;
    private final BudgetService budgetService;

    @Override
    public FinancialContext getMonthlyContext(String userId,
                                              YearMonth month) {

        BigDecimal totalIncome = incomeService.getIncomeByMonth(userId, month);

        BigDecimal totalExpense = transactionService.getTotalExpense(userId, month);

        List<CategoryExpense> categoryExpenses =
                Optional.ofNullable(
                                transactionService.getCategoryExpenses(userId, month))
                        .orElse(List.of());

        List<BudgetStatus> budgetStatuses = Optional.ofNullable(
                                budgetService.getMonthlyBudgetsStatus(userId, month))
                                .orElse(List.of());

        BigDecimal totalSavings =
                totalIncome.subtract(totalExpense);

        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalSavings);

        System.out.println("Income = " + totalIncome);
        System.out.println("Expense = " + totalExpense);

        return FinancialContext.builder()
                .userId(userId)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalSavings)
                .categoryExpenses(categoryExpenses)
                .savingsRate(savingsRate)
                .budgetStatuses(budgetStatuses)
                .build();

        // comparisons for now
    }

    private BigDecimal calculateSavingsRate(
            BigDecimal income,
            BigDecimal savings) {

        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return savings.multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
