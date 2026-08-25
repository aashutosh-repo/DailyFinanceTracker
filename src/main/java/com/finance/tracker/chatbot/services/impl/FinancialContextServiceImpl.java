package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialContextService;
import com.finance.tracker.service.BudgetService;
import com.finance.tracker.service.impl.FinancialTransactionReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class FinancialContextServiceImpl implements FinancialContextService {
    private final BudgetService budgetService;
    private final FinancialTransactionReadService financialTransactionReadService;


    @Override
    public FinancialContext getMonthlyContext(String userId, YearMonth month) {
        // Income
        BigDecimal totalIncome =
                financialTransactionReadService.getTotalIncome(userId, month);
        // Expenses
        BigDecimal totalExpense =
                financialTransactionReadService.getTotalExpense(userId, month);
        FinancialContext context = FinancialContext.builder()
                .userId(userId)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalIncome.subtract(totalExpense))
                .categoryExpenses(financialTransactionReadService.getCategoryExpense(userId, month))
                .budgetStatuses(budgetService.getMonthlyBudgetsStatus(userId,month))
                .build();
        return context;
    }
}
