package com.finance.tracker.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Expense Summary DTO (for dashboard)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ExpenseSummary {

    private BigDecimal totalExpense;
    private BigDecimal averageExpense;
    private Long totalTransactions;
    private LocalDate startDate;
    private LocalDate endDate;
}
