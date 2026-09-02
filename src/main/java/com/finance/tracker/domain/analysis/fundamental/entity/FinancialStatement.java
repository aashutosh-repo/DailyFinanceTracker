package com.finance.tracker.domain.analysis.fundamental.entity;

import com.finance.tracker.stock.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "financial_statement")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialStatement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private int fiscalYear;

    @Column(nullable = false)
    private int fiscalQuarter;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal revenue;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal operatingIncome;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal ebit;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal interestExpense;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal taxes;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal netIncome;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal operatingCashFlow;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal capitalExpenditures;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal freeCashFlow;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal totalAssets;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal totalLiabilities;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal totalEquity;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal totalDebt;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal cash;

    @Column(columnDefinition = "NUMERIC(18,2)")
    private BigDecimal workingCapital;

    @Column(length = 50)
    private String source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
}
