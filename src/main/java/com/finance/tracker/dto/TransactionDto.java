package com.finance.tracker.dto;
import com.finance.tracker.constants.ExpenseType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal txnAmount;
    private ExpenseType expenseCategory;
    private String txnType;
    private LocalDate dateOfExpense;
    private String description;
}
