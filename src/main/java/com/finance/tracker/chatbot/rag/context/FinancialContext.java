package com.finance.tracker.chatbot.rag.context;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public class FinancialContext {

    private Long userId;
    private YearMonth month;

    // Income

    private BigDecimal totalIncome;

    // Expense
    private BigDecimal totalExpense;
    private List<CategoryExpense> categoryExpenses;

    // Savings
    private BigDecimal totalSavings;

    // Budgets
    private List<BudgetStatus> budgetStatuses;

    // Goals
//    private List<SavingsGoalContext> savingsGoals;

    // Alerts
//    private List<BudgetAlertContext> alerts;

    // Historical comparison
    private List<MonthlyComparison> comparisons;

}