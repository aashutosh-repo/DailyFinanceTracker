package com.finance.tracker.dto.expense;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expense Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String currency;
    private LocalDate expenseDate;
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private String receiptUrl;
    private Boolean isRecurring;
    private ExpenseCategoryResponse category;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
