package com.finance.tracker.repository;
import com.finance.tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
//    List<Transaction> findByUserIdAndTxnDateBetween(UUID userId, LocalDate from, LocalDate to);
    List<Transaction> findByExtUserId(String userId);
    @Query("""
        SELECT COALESCE(SUM(t.txnAmount), 0) FROM Transaction t
        WHERE t.extUserId = :userId AND t.txnType = 'DEBIT' AND t.dateOfExpense BETWEEN :startDate AND :endDate
    """)
    BigDecimal getTotalExpense(@Param("userId") String userId, @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT t.typeOfExpense, COALESCE(SUM(t.txnAmount), 0) FROM Transaction t WHERE t.extUserId = :userId
        AND t.txnType = 'DEBIT' AND t.dateOfExpense BETWEEN :startDate AND :endDate GROUP BY t.typeOfExpense
    """)
    List<Object[]> getCategoryExpenses(@Param("userId") String userId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT t FROM Transaction t WHERE t.extUserId = :userId
        AND t.txnType = 'DEBIT' AND t.dateOfExpense BETWEEN :startDate AND :endDate ORDER BY t.dateOfExpense DESC
    """)
    List<Transaction> getExpensesByMonth(
                                        @Param("userId") String userId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}
