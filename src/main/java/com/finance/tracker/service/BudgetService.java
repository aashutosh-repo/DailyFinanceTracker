package com.finance.tracker.service;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.dto.budget.BudgetRequest;
import com.finance.tracker.dto.budget.BudgetResponse;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(BudgetRequest request, String userId);
    BudgetResponse updateBudget(Long budgetId, BudgetRequest request);
    BudgetResponse getBudgetById(Long budgetId);
    List<BudgetResponse> getBudgetByUserId(String userID);
    List<BudgetResponse> getBudgetsByUser(String userId);
    void deleteBudget(Long budgetId);
    BigDecimal getMonthlyBudgets(String userId, YearMonth month);
    List<BudgetStatus> getMonthlyBudgetsStatus(String userId, YearMonth month);
}
