package com.finance.tracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * IncomeSource Entity - Categories for income sources
 */
@Entity
@Table(name = "income_sources", indexes = {
    @Index(name = "idx_income_sources_user_id", columnList = "user_id"),
    @Index(name = "idx_income_sources_deleted_at", columnList = "deleted_at")
}, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeSource extends BaseEntity {
    
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
}
