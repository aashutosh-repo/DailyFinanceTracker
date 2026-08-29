package com.finance.tracker.stock.company;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "companies",
        indexes = {
                @Index(name = "idx_company_symbol", columnList = "symbol")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 30
    )
    private String symbol;

    @Column(
            nullable = false,
            length = 255
    )
    private String name;

    @Column(length = 100)
    private String exchange;

    @Column(length = 100)
    private String sector;

    @Column(length = 100)
    private String industry;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
