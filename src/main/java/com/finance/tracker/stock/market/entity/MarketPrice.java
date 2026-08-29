package com.finance.tracker.stock.market.entity;

import com.finance.tracker.stock.company.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_prices",
        indexes = {
                @Index(
                        name = "idx_market_price_company_date",
                        columnList = "company_id, price_date"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_market_price_company_date",
                        columnNames = {
                                "company_id",
                                "price_date"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false
    )
    private Company company;

    @Column(name = "price_date", nullable = false)
    private LocalDate priceDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal openPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal highPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal lowPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal closePrice;

    private Long volume;

    @Column(length = 50)
    private String source;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}