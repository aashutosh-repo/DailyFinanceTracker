package com.finance.tracker.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense Filter DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ExpenseFilter {

    private LocalDate startDate;
    private LocalDate endDate;
    private Long categoryId;
    private String paymentMethod;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private List<String> tags;
}
