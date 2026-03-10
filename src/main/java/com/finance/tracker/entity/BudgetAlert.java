package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Budget Alert Entity
 * Represents alerts triggered when budget thresholds are exceeded
 */
@Entity
@Table(name = "budget_alerts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlert extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;
    
    @Column(nullable = false)
    private BigDecimal thresholdPercentage;
    
    @Column(nullable = false)
    private BigDecimal currentSpent;
    
    @Column(nullable = false)
    private LocalDateTime alertTriggeredAt;
    
    @Column(length = 500)
    private String message;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isAcknowledged = false;
}
