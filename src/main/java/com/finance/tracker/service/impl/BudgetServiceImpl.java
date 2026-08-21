package com.finance.tracker.service.impl;

import com.finance.tracker.chatbot.rag.context.BudgetStatus;
import com.finance.tracker.constants.BudgetType;
import com.finance.tracker.constants.TransactionCategory;
import com.finance.tracker.dto.budget.BudgetRequest;
import com.finance.tracker.dto.budget.BudgetResponse;
import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.User;
import com.finance.tracker.events.ChangeType;
import com.finance.tracker.events.FinancialDataChangedEvent;
import com.finance.tracker.repository.BudgetRepository;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.BudgetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;
    private final FinancialTransactionReadService transactionReadService;

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BudgetType type = BudgetType.valueOf(request.getCategory());
        int id = type.getId();
        Budget budget = Budget.builder()
                .user(user)
                .extUserId(user.getUserId())
                .categoryId(id)
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
        eventPublisher.publishEvent(
                new FinancialDataChangedEvent(userId, YearMonth.from(saved.getStartDate()), ChangeType.BUDGET)
        );
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));

        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setCurrency(request.getCurrency());
        budget.setAlertThreshold(request.getAlertThreshold());
        budget.setAlertFrequency(request.getAlertFrequency());
        
        Budget updated = budgetRepository.save(budget);
        eventPublisher.publishEvent(
                new FinancialDataChangedEvent(updated.getExtUserId(), YearMonth.from(updated.getStartDate()),ChangeType.BUDGET
                )
        );
        return mapToResponse(updated);
    }

    @Override
    public BudgetResponse getBudgetById(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        return mapToResponse(budget);
    }

    @Override
    public List<BudgetResponse> getBudgetByUserId(String userId) {
        BigDecimal budgetAmt = getMonthlyBudgets(userId, YearMonth.now());
        Optional<User> user = userRepository.findByUserId(userId);
        if(user.isEmpty()){
            return new ArrayList<>();
        }
       List<Budget> budget = budgetRepository.findByUser(user.get());
       List<BudgetResponse> responses = new ArrayList<>();
       for (Budget b : budget){
           responses.add(mapToResponse(b));
       }
        return responses;
    }

    @Override
    public List<BudgetResponse> getBudgetsByUser(String userId) {
        return getBudgetByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget Not Found"));
        budgetRepository.deleteById(budgetId);
        eventPublisher.publishEvent(
                new FinancialDataChangedEvent(budget.getExtUserId(), YearMonth.from(budget.getStartDate()), ChangeType.BUDGET)
        );
    }

    @Override
    public BigDecimal getMonthlyBudgets(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return budgetRepository.getMonthlyBudget(userId, "MONTHLY", start, end);
    }

    public List<BudgetStatus> getMonthlyBudgetsStatus(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<Budget> availableBudget = budgetRepository.getMonthlyBudgetStatus(userId, "MONTHLY", start, end);

        Map<Integer, BigDecimal> actualByCategory = transactionReadService.getExpenseTotalsByBudgetCategoryId(userId, month);
        List<BudgetStatus> statusList = new ArrayList<>();

        for(Budget b : availableBudget){

            BudgetType type = BudgetType.fromId(b.getCategoryId());
            BigDecimal actual = actualByCategory.getOrDefault(b.getCategoryId(), BigDecimal.ZERO);
            boolean exceeded = actual.compareTo(b.getAmount())> 0;

            if(exceeded) {
                log.warn("Budget exceeded for UserId {} category {} limit {} actual {}", userId, type.name(), b.getAmount(), actual);
            }
            statusList.add(new BudgetStatus(type.name(), b.getAmount(), actual,exceeded));
        }
        return statusList;
    }

    private BudgetResponse mapToResponse(Budget budget) {
        BigDecimal currentSpending = BigDecimal.ZERO;
        BigDecimal percentageUsed = BigDecimal.ZERO;
        String budgetStatus = "SAFE";

        if(budget.getCategoryId() != null) {
            currentSpending = transactionReadService.getExpenseTotalForCategory(budget.getExtUserId(),
                    Long.valueOf(budget.getCategoryId()),
                    budget.getStartDate(),
                    budget.getEndDate());

            if (budget.getAmount().compareTo(BigDecimal.ZERO) >0 ) {
               percentageUsed = currentSpending.multiply(BigDecimal.valueOf(100))
                       .divide(budget.getAmount(),2, RoundingMode.HALF_UP);
            }

            if (currentSpending.compareTo(budget.getAmount()) > 0) {
                budgetStatus = "EXCEEDED";
            } else if (percentageUsed.compareTo(budget.getAlertThreshold() != null ? budget.getAlertThreshold() : BigDecimal.valueOf(80)) >=0) {
                budgetStatus = "WARNING";
            }
        }

        BudgetType type = BudgetType.fromId(budget.getCategoryId());

        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryName(type.name())
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
