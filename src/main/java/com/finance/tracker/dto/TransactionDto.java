package com.finance.tracker.dto;
import com.finance.tracker.constants.ExpenseType;
import com.finance.tracker.constants.TransactionCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDto {
    private Long id;

    @NotBlank(message = "UserId is required")
    private String userId;
    private Long categoryId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01",message = "Amount must be greater than 0")
    private BigDecimal txnAmount;
    private TransactionCategory expenseCategory;

    @NotBlank(message = "Transaction type is required")
    private String txnType;

    @NotNull(message = "Date is required")
    private LocalDate dateOfExpense;

    @Size(max = 500, message = "Description must not be Null")
    private String description;
}
