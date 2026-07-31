package com.finance.tracker.service.impl;

import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.constants.IncomeSource;
import com.finance.tracker.constants.TransactionCategory;
import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.entity.*;
import com.finance.tracker.events.ChangeType;
import com.finance.tracker.events.FinancialDataChangedEvent;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.mapper.TransactionMapper;
import com.finance.tracker.repository.*;
import com.finance.tracker.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public TransactionDto addExpense(TransactionDto dto) {
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + dto.getUserId()));
        String normalizedTxnType = dto.getTxnType() == null ? "DEBIT" : dto.getTxnType().trim().toUpperCase();
        dto.setTxnType(normalizedTxnType);

        Transaction expense = TransactionMapper.toEntity(dto, user);
        if(expense.getTypeOfExpense()==null){
            expense.setTypeOfExpense(TransactionCategory.OTHER);
        }

        Transaction saved = transactionRepository.save(expense);

        dto.setId(saved.getId());
        
        // Sync to EXPENSES or INCOME table based on transaction type
        syncTransactionToDetailTable(dto, user);

        ChangeType changeType = "CREDIT".equals(normalizedTxnType) ? ChangeType.INCOME: ChangeType.TRANSACTION;
        dto.setTxnType(normalizedTxnType);

        eventPublisher.publishEvent(
                new FinancialDataChangedEvent(user.getUserId(), YearMonth.from(saved.getDateOfExpense()), ChangeType.TRANSACTION)
        );
        
        return dto;
    }
    
    /**
     * Sync transaction to EXPENSES or INCOME table based on txnType
     */
    private void syncTransactionToDetailTable(TransactionDto dto, User user) {
        try {
            if ("DEBIT".equalsIgnoreCase(dto.getTxnType())) {
                // Create Expense record
                createExpenseFromTransaction(dto, user);
                log.info("Synced DEBIT transaction to EXPENSES table for user: {}", user.getId());
            } else if ("CREDIT".equalsIgnoreCase(dto.getTxnType())) {
                // Create Income record
                createIncomeFromTransaction(dto, user);
                log.info("Synced CREDIT transaction to INCOME table for user: {}", user.getId());
            } else {
                log.warn("Unknown transaction type: {}", dto.getTxnType());
            }
        } catch (Exception e) {
            log.error("Error syncing transaction to detail table for user: {}", user.getId(), e);
            // Log but don't fail - transaction is already saved
        }
    }
    
    /**
     * Create Expense record from DEBIT transaction
     */
    private void createExpenseFromTransaction(TransactionDto dto, User user) {
        // Get category - use provided categoryId or fetch default
//        ExpenseCategory category;
//        if (dto.getCategoryId() != null) {
//            category = expenseCategoryRepository
//                    .findByIdAndUserIdAndDeletedAtIsNull(dto.getCategoryId(), user.getUserId())
//                    .orElseThrow(() -> new ResourceNotFoundException(
//                        "Category not found for user: " + dto.getCategoryId()
//                    ));
//        } else {
//            // Fallback to "Other" category if available
//            category = expenseCategoryRepository
//                    .findByUserIdAndNameAndDeletedAtIsNull(user.getUserId(), "Other")
//                    .orElseThrow(() -> new ResourceNotFoundException(
//                        "Default 'Other' category not found for user"
//                    ));
//        }
        
        Expense expense = Expense.builder()
                .user(user)
//                .category(category)
                .description(dto.getDescription())
                .amount(dto.getTxnAmount())
                .currency("USD")
                .expenseDate(dto.getDateOfExpense())
                .isRecurring(false)
                .build();
        
        expenseRepository.save(expense);
    }
    
    /**
     * Create Income record from CREDIT transaction
     */
    private void createIncomeFromTransaction(TransactionDto dto, User user) {
        // Map expense category to income source
        Income income = Income.builder()
                .user(user)
                .sourceType(IncomeSource.OTHER)
                .description(dto.getDescription())
                .amount(dto.getTxnAmount())
                .currency("USD")
                .incomeDate(dto.getDateOfExpense())
                .isRecurring(false)
                .build();
        
        incomeRepository.save(income);
    }

    @Override
    public List<TransactionDto> getAllExpensesByUser(String userId) {
        return transactionRepository.findByExtUserId(userId)
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    @Override
    public TransactionDto getExpenseById(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
        return TransactionMapper.toDto(t);
    }

    @Override
    public void deleteExpense(Long id) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense Not Found with Id " + id));
        transactionRepository.deleteById(id);
        eventPublisher.publishEvent(
                new FinancialDataChangedEvent(txn.getExtUserId(), YearMonth.from(txn.getDateOfExpense()), ChangeType.TRANSACTION)
        );

    }

    @Override
    public BigDecimal getTotalExpense(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository.getTotalExpense(userId, start, end);
    }

    @Override
    public List<CategoryExpense> getCategoryExpenses(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository
            .getCategoryExpenses(userId, start, end)
                .stream()
                .map(row -> new CategoryExpense(
                        ((TransactionCategory) row[0]).name(),
                        (BigDecimal) row[1]
                ))
                .toList();
    }

    @Override
    public List<TransactionDto> getExpensesByMonth(String userId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository
                .getExpensesByMonth(userId, start, end)
                .stream()
                .map(TransactionMapper::toDto)
                .toList();
    }
}
