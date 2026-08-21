package com.finance.tracker.domain.transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialTransactionRepository {
    FinancialTransaction save(FinancialTransaction transaction);
    void delete(FinancialTransaction transaction);

    Optional<FinancialTransaction> findById(TransactionId id);

    List<FinancialTransaction> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    List<FinancialTransaction> findActiveByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    List<FinancialTransaction> findByUserAndTypeAndDateRange(Long userId, TransactionType type, LocalDate startDate, LocalDate endDate);
    List<FinancialTransaction> findByUserAndCategoryAndDateRange(Long userId, Long categoryId, LocalDate startDate, LocalDate endDate);
    boolean existById(TransactionId id);
}
