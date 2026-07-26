package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialContextService;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.IncomeService;
import com.finance.tracker.service.TransactionService;
import com.finance.tracker.service.impl.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class FinancialContextServiceImpl implements FinancialContextService {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final BudgetService budgetService;
    private final TransactionService transactionService;

    @Override
    public FinancialContext getMonthlyContext(String userId, YearMonth month) {
        // Income
        BigDecimal totalIncome =
                incomeService.getIncomeByMonth(userId, month);
        // Expenses
        BigDecimal totalExpense =
                transactionService.getTotalExpense(userId, month);
        FinancialContext context = FinancialContext.builder()
                .userId(userId)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalIncome.subtract(totalExpense))
                .categoryExpenses(transactionService.getCategoryExpenses(userId, month))
                .budgetStatuses(budgetService.getMonthlyBudgetsStatus(userId,month))
                .build();
        return context;
    }
}
