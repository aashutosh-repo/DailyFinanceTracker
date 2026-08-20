package com.finance.tracker.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTransactionCommand {
    private Long transactionId;
    private Long userId;

    //fin data
    private String amount;
    private String currency;
    private String description;
    private Long categoryId;
    private String paymentMethod;
    private String receiptUrl;
    private String incomeSource;

    //Audit
    private String createdBy;

}
