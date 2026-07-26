package com.finance.tracker.chatbot.rag.context;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record FinancialContext(
        String userId,
        YearMonth month,

        // Income
        BigDecimal totalIncome,

        // Expense
        BigDecimal totalExpense,
        List<CategoryExpense> categoryExpenses,

        // Savings
        BigDecimal totalSavings,

        // Budgets
        List<BudgetStatus> budgetStatuses,



        // Historical comparison
        List<MonthlyComparison> comparisons

) {}