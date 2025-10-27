package com.finance.tracker.service;

import com.finance.tracker.dto.TransactionDto;

import java.util.List;

public interface TransactionService {
    TransactionDto addExpense(TransactionDto expenseDto);
    List<TransactionDto> getAllExpensesByUser(Long userId);
    TransactionDto getExpenseById(Long id);
    void deleteExpense(Long id);
}
