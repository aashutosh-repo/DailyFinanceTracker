package com.finance.tracker.dto.expense;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Expense Category Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseCategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private String colorCode;
    private Boolean isDefault;
    private BigDecimal monthlyBudget;
}
