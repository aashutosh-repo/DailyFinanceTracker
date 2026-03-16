package com.finance.tracker.service;

import com.finance.tracker.dto.budget.BudgetRequest;
import com.finance.tracker.dto.budget.BudgetResponse;
import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(BudgetRequest request, Long userId);
    BudgetResponse updateBudget(Long budgetId, BudgetRequest request);
    BudgetResponse getBudgetById(Long budgetId);
    List<BudgetResponse> getBudgetsByUser(Long userId);
    void deleteBudget(Long budgetId);
}
