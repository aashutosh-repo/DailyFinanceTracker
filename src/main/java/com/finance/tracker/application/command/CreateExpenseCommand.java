package com.finance.tracker.application.command;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateExpenseCommand {
    private Long userId;

    //fin data
    private String amount;
    private String currency;
    private LocalDate transactionDate;

    //categorization
    private Long categoryId;
    private String description;

    //Type
    private String paymentMethod;
    private String receiptUrl;

    //Audit

    private String createdBy;

}
