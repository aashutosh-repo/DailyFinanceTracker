package com.finance.tracker.chatbot.services.impl;

import com.finance.tracker.chatbot.rag.context.FinancialContext;
import com.finance.tracker.chatbot.services.FinancialContextService;
import com.finance.tracker.service.TransactionService;
import com.finance.tracker.service.impl.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class FinancialContextServiceImpl implements FinancialContextService {
    private final ExpenseService expenseService;
//    private final IncomeService incomeService;
//    private final BudgetService budgetService;
    private final TransactionService transactionService;

    @Override
    public FinancialContext getMonthlyContext(Long userId, YearMonth month) {
        return null;
    }
}
