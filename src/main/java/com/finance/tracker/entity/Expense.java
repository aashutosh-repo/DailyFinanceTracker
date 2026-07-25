package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Expense Entity - User expense tracking
 */
@Entity
@Table(name = "expenses", indexes = {
    @Index(name = "idx_expenses_user_id", columnList = "user_id"),
    @Index(name = "idx_expenses_expense_date", columnList = "expense_date"),
    @Index(name = "idx_expenses_user_date", columnList = "user_id,expense_date"),
    @Index(name = "idx_expenses_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(length = 500)
    private String description;
    
    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency = "USD";
    
    @NotNull
    @Column(nullable = false)
    private LocalDate expenseDate;
    
    @Column(length = 50)
    private String paymentMethod;
    
    @Column(length = 100)
    private String referenceNumber;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(columnDefinition = "TEXT")
    private String receiptUrl;
    
    @Column(nullable = false)
    private Boolean isRecurring = false;
    
    @Column(name = "recurring_expense_id")
    private Long recurringExpenseId;
    
    // Relationships
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ExpenseTag> tags = new HashSet<>();
    
    @PostUpdate
    @PostPersist
    protected void updateTransactionTable() {
        // Implement transaction sync logic
    }
}
