package com.finance.tracker.application.query;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetTransactionQuery {
    private Long transactionId;
    private Long userId;
}
