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
public class CreateTransferCommand {
    private Long userId;

    //fin data
    private String amount;
    private String currency;
    private LocalDate transactionDate;

    //Transfer-specific
    private Long sourceAccount;
    private Long destinationAccount;
    private String description;

    //Audit

    private String createdBy;

}
