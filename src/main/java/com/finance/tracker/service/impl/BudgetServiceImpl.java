package com.finance.tracker.service.impl;

import com.finance.tracker.dto.budget.BudgetRequest;
import com.finance.tracker.dto.budget.BudgetResponse;
import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.ExpenseCategory;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.ExpenseCategoryRepository;
import com.finance.tracker.repository.ExpenseRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public BudgetResponse createBudget(BudgetRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        ExpenseCategory category = expenseCategoryRepository
            .findByIdAndUserIdAndDeletedAtIsNull(request.getCategoryId(), userId)
            .orElseThrow(() -> new RuntimeException(
                "Category not found for this user with ID: " + request.getCategoryId()
            ));
        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .name(request.getName())
                .amount(request.getAmount())
                .period(request.getPeriod())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .alertThreshold(request.getAlertThreshold())
                .alertFrequency(request.getAlertFrequency())
                .isActive(true)
                .build();
        
        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    @Override
    public BudgetResponse updateBudget(Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        
        ExpenseCategory category = expenseCategoryRepository
    .findByIdAndUserIdAndDeletedAtIsNull(request.getCategoryId(), budget.getUser().getId())
    .orElseThrow(() -> new RuntimeException(
        "Category not found for this user with ID: " + request.getCategoryId()
    ));

        budget.setCategory(category);
        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setCurrency(request.getCurrency());
        budget.setAlertThreshold(request.getAlertThreshold());
        budget.setAlertFrequency(request.getAlertFrequency());
        
        Budget updated = budgetRepository.save(budget);
        return mapToResponse(updated);
    }

    @Override
    public BudgetResponse getBudgetById(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        return mapToResponse(budget);
    }

    @Override
    public List<BudgetResponse> getBudgetsByUser(Long userId) {
        List<Budget> budgets = budgetRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        return budgets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteBudget(Long budgetId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new RuntimeException("Budget not found");
        }
        budgetRepository.deleteById(budgetId);
    }

    private BudgetResponse mapToResponse(Budget budget) {
        BigDecimal currentSpending = BigDecimal.ZERO;
        BigDecimal percentageUsed = BigDecimal.ZERO;
        String budgetStatus = "SAFE";
        
        // Calculate current spending for this budget's category within the budget period
        if (budget.getCategory() != null) {
            currentSpending = expenseRepository.sumExpensesByCategoryAndDateRange(
                    budget.getUser().getId(),
                    budget.getCategory().getId(),
                    budget.getStartDate(),
                    budget.getEndDate()
            );
            
            // Calculate percentage used
            if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                percentageUsed = currentSpending
                        .multiply(BigDecimal.valueOf(100))
                        .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);
            }
            
            // Determine budget status
            if (currentSpending.compareTo(budget.getAmount()) > 0) {
                budgetStatus = "EXCEEDED";
            } else if (percentageUsed.compareTo(budget.getAlertThreshold() != null ? budget.getAlertThreshold() : BigDecimal.valueOf(80)) >= 0) {
                budgetStatus = "WARNING";
            }
        }
        
        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .name(budget.getName())
                .amount(budget.getAmount())
                .period(budget.getPeriod())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .currency(budget.getCurrency())
                .alertThreshold(budget.getAlertThreshold())
                .alertFrequency(budget.getAlertFrequency())
                .isActive(budget.getIsActive())
                .currentSpending(currentSpending)
                .percentageUsed(percentageUsed)
                .budgetStatus(budgetStatus)
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}
