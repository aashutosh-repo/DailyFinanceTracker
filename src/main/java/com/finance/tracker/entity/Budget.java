package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Budget Entity - User budget management
 */
@Entity
@Table(name = "budgets", indexes = {
    @Index(name = "idx_budgets_category_id", columnList = "category_id"),
    @Index(name = "idx_budgets_is_active", columnList = "is_active"),
    @Index(name = "idx_budgets_period_dates", columnList = "user_id,start_date,end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String extUserId;
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;

    @NotNull(message = "CategoryId cannot be Null")
    @Column(nullable = false)
    private Integer categoryId;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String period; // MONTHLY, QUARTERLY, YEARLY
    
    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;
    
    @DecimalMin("1")
    @DecimalMax("100")
    @Column(precision = 3, scale = 0)
    private BigDecimal alertThreshold = BigDecimal.valueOf(80);
    
    @Column(length = 50)
    private String alertFrequency = "WEEKLY";
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    /**
     * Check if budget period is valid
     */
    @Transient
    public boolean isPeriodValid() {
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }
    
    /**
     * Get budget status: within limit or exceeded
     */
    @Transient
    public String getBudgetStatus(BigDecimal currentSpending) {
        if (currentSpending.compareTo(amount) > 0) {
            return "EXCEEDED";
        } else if (currentSpending.multiply(BigDecimal.valueOf(100))
                .divide(amount, 2, java.math.RoundingMode.HALF_UP)
                .compareTo(alertThreshold) >= 0) {
            return "WARNING";
        }
        return "SAFE";
    }
}
