package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * ExpenseTag Entity - Tags for expenses
 */
@Entity
@Table(name = "expense_tags", indexes = {
    @Index(name = "idx_expense_tags_expense_id", columnList = "expense_id"),
    @Index(name = "idx_expense_tags_tag_name", columnList = "tag_name")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"expense_id", "tag_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseTag {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;
    
    @Column(nullable = false, length = 100)
    private String tagName;
    
    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();
}
