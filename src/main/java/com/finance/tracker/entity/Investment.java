package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Investment Entity - Stock, Mutual Funds, SIP tracking
 */
@Entity
@Table(name = "investments", indexes = {
    @Index(name = "idx_investments_user_id", columnList = "user_id"),
    @Index(name = "idx_investments_status", columnList = "status"),
    @Index(name = "idx_investments_type", columnList = "investment_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String extUserId;
    
    @NotBlank
    @Column(nullable = false, length = 50)
    private String investmentType; // STOCKS, MUTUAL_FUNDS, SIP, CRYPTO, BONDS, FIXED_DEPOSIT
    
    @NotBlank
    @Column(nullable = false, length = 255)
    private String name;
    
    @Column(length = 20)
    private String tickerSymbol;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate buyDate;
    
    @NotNull
    @DecimalMin("0.0001")
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal buyPrice;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @Column(length = 255)
    private String brokerName;
    
    @NotNull
    @Column(nullable = false, length = 50)
    private String status; // ACTIVE, SOLD, MATURED
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    /**
     * Calculate current portfolio value
     */
    @Transient
    public BigDecimal getCurrentValue() {
        return quantity.multiply(currentPrice);
    }
    
    /**
     * Calculate gain/loss
     */
    @Transient
    public BigDecimal getGainLoss() {
        return getCurrentValue().subtract(
            quantity.multiply(buyPrice)
        );
    }
    
    /**
     * Calculate gain/loss percentage
     */
    @Transient
    public BigDecimal getGainLossPercentage() {
        BigDecimal investedAmount = quantity.multiply(buyPrice);
        if (investedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getGainLoss()
            .multiply(BigDecimal.valueOf(100))
            .divide(investedAmount, 2, java.math.RoundingMode.HALF_UP);
    }
}
