package com.finance.tracker.entity;

import com.finance.tracker.constants.IncomeSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Income Entity - User income tracking
 */
@Entity
@Table(name = "income", indexes = {
    @Index(name = "idx_income_user_id", columnList = "user_id"),
    @Index(name = "idx_income_source_type", columnList = "source_type"),
    @Index(name = "idx_income_income_date", columnList = "income_date"),
    @Index(name = "idx_income_user_date", columnList = "user_id,income_date"),
    @Index(name = "idx_income_deleted_at", columnList = "deleted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10)
    private String extUserId;

    @NotNull
    @Column(name = "source_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private IncomeSource sourceType;

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
    private LocalDate incomeDate;

    @Column(length = 100)
    private String referenceNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "recurring_income_id")
    private Long recurringIncomeId;
}
