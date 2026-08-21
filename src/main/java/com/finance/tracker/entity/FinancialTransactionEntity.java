package com.finance.tracker.entity;


import com.finance.tracker.domain.transaction.TransactionStatus;
import com.finance.tracker.domain.transaction.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "financial_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialTransactionEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    private TransactionType type;
    private TransactionStatus status;

    private BigDecimal amount;
    private String currency;

    private LocalDate transactionDate;
    private Long categoryId;
    private String description;

    private String incomeSource;
    private String paymentMethod;
    private String receiptUrl;
    private Set<String> tags;
    private Long sourceAccountId;
    private Long destinationAccountId;

    private String investmentType;

    private Long investmentId;
    private BigDecimal quantity;
    private BigDecimal price;
}
