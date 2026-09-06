package com.finance.tracker.stock.market.entity;

import com.finance.tracker.stock.company.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "market_quotes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_market_quote_company", columnNames = "company_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketQuote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(precision = 19, scale = 4)
    private BigDecimal change;

    @Column(precision = 19, scale = 4)
    private BigDecimal changePercent;

    @Column(precision = 19, scale = 4)
    private BigDecimal open;

    @Column(precision = 19, scale = 4)
    private BigDecimal high;

    @Column(precision = 19, scale = 4)
    private BigDecimal low;

    private Long volume;

    private LocalDateTime marketTime;
    private LocalDateTime fetchedAt;

    @Column(length = 50)
    private String source;
}
