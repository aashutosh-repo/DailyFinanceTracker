package com.finance.tracker.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TransactionDto {
    private UUID id;
    private UUID userId;
    private Long categoryId;
    private BigDecimal amount;
    private String txnType;
    private LocalDate txnDate;
    private String description;
}
