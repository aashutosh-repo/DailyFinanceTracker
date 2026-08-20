package com.finance.tracker.application.dto;

import com.finance.tracker.domain.transaction.TransactionStatus;
import com.finance.tracker.domain.transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDTO {
    //identity
    private Long id;
    private Long userId;

    //type, Status
    private TransactionType type;
    private TransactionStatus status;

    private String amount;
    private String currency;
    private LocalDate transactionDate;

    private Long categoryId;
    private String description;

    private LocalDate createAt;
    private LocalDate updateAt;
    private String createBy;
    private String updateBy;
    private LocalDate deleteAt;

    private String incomeSource;

    private  String paymentMethod;
    private  String receiptUrl;
    private Set<String> tags;

    private Long sourceAccountId;
    private Long destinationAccountId;

    private String investmentType;
    private Long investmentId;
    private BigDecimal quantity;
    private String price;

}
