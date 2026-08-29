package com.finance.tracker.stock.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository
        extends JpaRepository<Company, UUID> {

    Optional<Company> findBySymbol(String symbol);

    boolean existsBySymbol(String symbol);
    Optional<Company> findBySymbolIgnoreCase(String symbol);
}