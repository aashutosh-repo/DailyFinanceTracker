package com.finance.tracker.mapper;

import com.finance.tracker.dto.expense.ExpenseCategoryResponse;
import com.finance.tracker.dto.expense.ExpenseRequest;
import com.finance.tracker.dto.expense.ExpenseResponse;
import com.finance.tracker.entity.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Expense Mapper
 * Maps between Expense entities and DTOs
 */
@Component
@RequiredArgsConstructor
public class ExpenseMapper {
    
    /**
     * Convert Expense entity to ExpenseResponse DTO
     */
    public ExpenseResponse toResponse(Expense expense) {
        if (expense == null) {
            return null;
        }
        
        return ExpenseResponse.builder()
            .id(expense.getId())
            .description(expense.getDescription())
            .amount(expense.getAmount())
            .currency(expense.getCurrency())
            .expenseDate(expense.getExpenseDate())
            .paymentMethod(expense.getPaymentMethod())
            .referenceNumber(expense.getReferenceNumber())
            .notes(expense.getNotes())
            .receiptUrl(expense.getReceiptUrl())
            .isRecurring(expense.getIsRecurring())
            .createdAt(expense.getCreatedAt())
            .updatedAt(expense.getUpdatedAt())
            .build();
    }
    
    /**
     * Convert ExpenseRequest DTO to Expense entity (without category - to be set externally)
     */
    public Expense toEntity(ExpenseRequest request) {
        if (request == null) {
            return null;
        }
        
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCurrency(request.getCurrency());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setReferenceNumber(request.getReferenceNumber());
        expense.setNotes(request.getNotes());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setIsRecurring(request.getIsRecurring());
        
        return expense;
    }
}
