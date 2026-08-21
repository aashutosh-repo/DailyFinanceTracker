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
public class CreateTransferRequest {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency must required")
    @Size(min = 3, max = 3, message = "currency must be of 3 CHAR")
    private String currency;

    @NotNull(message = "transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in future")
    private LocalDate transactionDate;

    @NotNull(message = "source account Id cannot be null")
    @Positive(message = "source account id must me positive")
    private Long sourceAccountId;

    @NotNull(message = "destination account Id cannot be null")
    @Positive(message = "destination account id must me positive")
    private Long destinationAccountId;

    @NotBlank(message = "Transaction Description cannot be blank")
    @Size(max = 100, message = "max size more than 100 char of income source not allowed")
    private String description;

    @NotBlank(message = "created By required")
    @Size(max = 100, message = "max size >100 of Created By not allowed")
    private String createdBy;
}
