package com.finance.tracker.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Budget Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BudgetResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal amount;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private String currency;
    private BigDecimal alertThreshold;
    private String alertFrequency;
    private Boolean isActive;
    private BigDecimal currentSpending;
    private String budgetStatus; // SAFE, WARNING, EXCEEDED
    private BigDecimal percentageUsed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
