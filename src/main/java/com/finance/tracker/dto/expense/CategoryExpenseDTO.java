package com.finance.tracker.dto.expense;

import java.math.BigDecimal;

/**
 * DTO for category-wise expenses
 */
@lombok.Data
@lombok.Builder
public class CategoryExpenseDTO {
    private String categoryName;
    private BigDecimal amount;
    private BigDecimal percentage;
}
