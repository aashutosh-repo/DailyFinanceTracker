package com.finance.tracker.stock.market;

import com.finance.tracker.stock.market.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, UUID> {

    List<MarketPrice> findByCompanyIdOrderByPriceDateDesc(
            UUID companyId
    );

    List<MarketPrice> findByCompanyIdAndPriceDateBetweenOrderByPriceDateAsc(
            UUID companyId,
            LocalDate fromDate,
            LocalDate toDate
    );

    Optional<MarketPrice> findByCompanyIdAndPriceDate(UUID companyId, LocalDate priceDate);
    List<MarketPrice> findByCompanyIdAndPriceDateBetween(UUID companyId, LocalDate fromDate, LocalDate toDate);

}