package com.finance.tracker.dto;
import com.finance.tracker.constants.ExpenseType;
import com.finance.tracker.constants.TransactionCategory;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {
    private Long id;
    private String userId;
    private Long categoryId;
    private BigDecimal txnAmount;
    private TransactionCategory expenseCategory;
    private String txnType;
    private LocalDate dateOfExpense;
    private String description;
}
