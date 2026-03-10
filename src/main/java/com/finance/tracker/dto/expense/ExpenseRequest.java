package com.finance.tracker.dto.expense;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {
    
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
    
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
    
    private String currency = "USD";
    private String paymentMethod;
    private String referenceNumber;
    private String notes;
    private String receiptUrl;
    private Boolean isRecurring = false;
    private List<String> tags;
}

