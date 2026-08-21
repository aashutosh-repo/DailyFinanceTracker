package com.finance.tracker.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTransactionRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "category Id cannot be null")
    @Positive(message = "category id must me positive")
    private Long categoryId;

    @NotBlank(message = "Transaction Description cannot be blank")
    @Size(max = 100, message = "max size more than 100 char of income source not allowed")
    private String description;

    @NotBlank(message = "payment Method cannot be blank")
    @Size(max = 50, message = "max size of payment Method not allowed")
    private String paymentMethod;

    @NotBlank(message = "receipt URL cannot be blank")
    @Size(max = 1000, message = "max size than 1000 char Receipt URL not allowed")
    private String receiptUrl;

    @NotBlank(message = "income source cannot be blank")
    @Size(max = 50, message = "max size of income source not allowed")
    private String incomeSource;

    @NotBlank(message = "created By required")
    @Size(max = 100, message = "max size >100 of Created By not allowed")
    private String updatedBy;
}
