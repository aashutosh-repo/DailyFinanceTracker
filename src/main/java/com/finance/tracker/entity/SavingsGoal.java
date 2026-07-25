package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SavingsGoal Entity - Track savings goals and progress
 */
@Entity
@Table(name = "savings_goals", indexes = {
    @Index(name = "idx_savings_goals_status", columnList = "status"),
    @Index(name = "idx_savings_goals_target_date", columnList = "target_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoal extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
//    @NotNull
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
    @NotBlank
    @Column(nullable = false, length = 255)
    private String userId;
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String goalName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal targetAmount;
    
    @DecimalMin("0")
    @Column(precision = 15, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @NotNull
    @Column(nullable = false)
    private LocalDate targetDate;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String priority; // LOW, MEDIUM, HIGH
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String status; // ACTIVE, PAUSED, COMPLETED, CANCELLED
    
    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;
    
    /**
     * Get progress percentage
     */
    @Transient
    public BigDecimal getProgressPercentage() {
        if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount
            .multiply(BigDecimal.valueOf(100))
            .divide(targetAmount, 2, java.math.RoundingMode.HALF_UP);
    }
    
    /**
     * Get remaining amount to save
     */
    @Transient
    public BigDecimal getRemainingAmount() {
        BigDecimal remaining = targetAmount.subtract(currentAmount);
        return remaining.max(BigDecimal.ZERO);
    }
    
    /**
     * Check if goal is completed
     */
    @Transient
    public boolean isCompleted() {
        return currentAmount.compareTo(targetAmount) >= 0;
    }
}
