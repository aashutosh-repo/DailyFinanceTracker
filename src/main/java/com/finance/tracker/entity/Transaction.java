package com.finance.tracker.entity;

import com.finance.tracker.constants.ExpenseType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String extUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseType typeOfExpense;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate dateOfExpense;

    private BigDecimal txnAmount;

    private String txnType; // DEBIT / CREDIT
}
