package com.finance.tracker.dto.budget;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {
    
    @NotBlank(message = "Budget name is required")
    private String name;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    @NotNull(message = "Period is required")
    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY", message = "Period must be MONTHLY, QUARTERLY, or YEARLY")
    private String period;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private String currency = "USD";
    
    @DecimalMin("1")
    @DecimalMax("100")
    private BigDecimal alertThreshold = BigDecimal.valueOf(80);
    
    private String alertFrequency = "WEEKLY";
}

