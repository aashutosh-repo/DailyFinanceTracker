package com.finance.tracker.dto.budget;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Budget Alert Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
class BudgetAlertResponse {

    private Long id;
    private Long budgetId;
    private String alertType;
    private BigDecimal currentSpending;
    private BigDecimal budgetLimit;
    private BigDecimal percentageUsed;
    private Boolean isAcknowledged;
    private LocalDateTime createdAt;
}
