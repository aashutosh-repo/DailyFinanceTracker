package com.finance.tracker.entity;

import com.finance.tracker.constants.IncomeSource;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "income_details")
@Data
@RequiredArgsConstructor
public class IncomeDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source", nullable = false, length = 100)
    private IncomeSource source;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "income_date", nullable = false)
    private LocalDateTime incomeDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
