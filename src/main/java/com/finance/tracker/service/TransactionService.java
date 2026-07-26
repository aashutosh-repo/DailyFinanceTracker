package com.finance.tracker.service;

import com.finance.tracker.chatbot.rag.context.CategoryExpense;
import com.finance.tracker.dto.TransactionDto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface TransactionService {
    TransactionDto addExpense(TransactionDto expenseDto);
    List<TransactionDto> getAllExpensesByUser(String userId);
    TransactionDto getExpenseById(Long id);
    void deleteExpense(Long id);
    BigDecimal getTotalExpense(String userId, YearMonth month);
    List<CategoryExpense> getCategoryExpenses(String userId, YearMonth month);
    List<TransactionDto> getExpensesByMonth(String userId, YearMonth month);
}
