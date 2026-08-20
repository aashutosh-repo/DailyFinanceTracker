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
public class CreateIncomeCommand {
    private Long userId;

    //fin data
    private String amount;
    private String currency;
    private LocalDate transactionDate;

    //categorization
    private Long categoryId;
    private String description;

    //Type
    private String incomeSource;

    //Audit

    private String createdBy;

}
