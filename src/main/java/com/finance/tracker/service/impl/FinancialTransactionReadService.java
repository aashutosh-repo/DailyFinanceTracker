package com.finance.tracker.service.impl;

import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.constants.BudgetType;
import com.finance.tracker.domain.transaction.FinancialTransaction;
import com.finance.tracker.domain.transaction.FinancialTransactionRepository;
import com.finance.tracker.domain.transaction.TransactionStatus;
import com.finance.tracker.domain.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialTransactionReadService {
    private final FinancialTransactionRepository transactionRepository;
    private final AuthService userIdentityService;

    public BigDecimal getTotalIncome(String userId, YearMonth month) {
        return sumByType(userId, month, TransactionType.INCOME);
    }
    public BigDecimal getTotalExpense(String userId, YearMonth month) {
        return sumByType(userId, month, TransactionType.EXPENSE);
    }
    public List<CategoryExpense> getCategoryExpense(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return getExpenseTotalByCategoryId(userId, start, end).entrySet().stream()
                .map(entry -> new CategoryExpense(categoryName(entry.getKey()), entry.getValue()))
                .toList();
    }

    public Map<Integer, BigDecimal> getExpenseTotalsByBudgetCategoryId(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return getExpenseTotalByCategoryId(userId, start, end).entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().intValue(),
                        Map.Entry::getValue,
                        BigDecimal::add,
                        LinkedHashMap::new
                ));
    }

    public BigDecimal getExpenseTotalForCategory(String userId, Long categoryId, LocalDate startDate, LocalDate endDate) {
        if (categoryId == null){
            return BigDecimal.ZERO;
        }

        Long databaseUserId = userIdentityService.resolveDataBaseUserId(userId);

        return transactionRepository.findByUserAndCategoryAndDateRange(databaseUserId, categoryId, startDate, endDate).stream()
                .filter(this::isPosted)
                .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                .map(transaction -> transaction.getMoney().getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByType(String userId, YearMonth month, TransactionType transactionType) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        Long databaseUserId = userIdentityService.resolveDataBaseUserId(userId);

           return transactionRepository.findByUserAndTypeAndDateRange(databaseUserId, transactionType, start, end)
                   .stream()
                   .filter(this::isPosted)
                   .map(transaction -> transaction.getMoney().getAmount())
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isPosted(FinancialTransaction transaction) {
        return  transaction.getStatus()==null || transaction.getStatus() == TransactionStatus.POSTED;
    }

    private Map<Long, BigDecimal> getExpenseTotalByCategoryId(String userId, LocalDate start, LocalDate end) {
        Long databaseUserId = userIdentityService.resolveDataBaseUserId(userId);

        return transactionRepository.findByUserAndTypeAndDateRange(databaseUserId, TransactionType.EXPENSE, start, end)
                .stream()
                .filter(this::isPosted)
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategoryId() !=null ? transaction.getCategoryId(): 0L,
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                transaction -> transaction.getMoney().getAmount(),
                                BigDecimal::add
                        )
                ));
    }


    private String categoryName(Long categoryId) {
        if (categoryId == null || categoryId <=0) {
            return "OTHER";
        }
        try {
            return BudgetType.fromId(categoryId.intValue()).name();
        } catch (Exception e) {
            return "CATEGORY_"+categoryId;
        }
    }

}
