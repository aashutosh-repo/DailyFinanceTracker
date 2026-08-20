package com.finance.tracker.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteTransactionCommand {
    private Long transactionId;
    private Long userId;


    private String reason;
    
    //Audit
    private String createdBy;

}
