package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * ExpenseCategory Entity - Categories for expenses
 */
@Entity
@Table(name = "expense_categories", indexes = {
    @Index(name = "idx_expense_categories_user_id", columnList = "user_id"),
    @Index(name = "idx_expense_categories_deleted_at", columnList = "deleted_at")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCategory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String iconUrl;
    
    @Column(length = 7)
    private String colorCode;
    
    @Column(nullable = false)
    private Boolean isDefault = false;
    
    @DecimalMin("0.01")
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyBudget;
    
    // Relationships
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Expense> expenses = new HashSet<>();
}
