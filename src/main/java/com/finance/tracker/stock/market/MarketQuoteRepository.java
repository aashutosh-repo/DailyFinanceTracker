package com.finance.tracker.stock.market;

import com.finance.tracker.stock.market.entity.MarketQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MarketQuoteRepository extends JpaRepository<MarketQuote, UUID> {
    Optional<MarketQuote> findByCompanyId(UUID companyId);
}
