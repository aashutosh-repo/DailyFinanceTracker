package com.finance.tracker.mapper;

import com.finance.tracker.dto.TransactionDto;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.User;


public class TransactionMapper {

    public static TransactionDto toDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser() != null ? transaction.getUser().getId() : null);
        dto.setTxnAmount(transaction.getTxnAmount());
        dto.setExpenseCategory(transaction.getTypeOfExpense());
        dto.setTxnType(transaction.getTxnType());
        dto.setDateOfExpense(transaction.getDateOfExpense());
        dto.setDescription(transaction.getDescription());
        dto.setCategoryId(null); // category not present in Transaction entity
        return dto;
    }

    public static Transaction toEntity(TransactionDto dto, User user) {
        if (dto == null) {
            return null;
        }

        Transaction transaction = new Transaction();
        transaction.setId(dto.getId());
        transaction.setUser(user);
        transaction.setTxnAmount(dto.getTxnAmount());
        transaction.setTxnType(dto.getTxnType());
        transaction.setTypeOfExpense(dto.getExpenseCategory());
        transaction.setDateOfExpense(dto.getDateOfExpense());
        transaction.setDescription(dto.getDescription());
        // ExpenseType could be derived if necessary
//        transaction.setTypeOfExpense(null);

        return transaction;
    }
}

