package com.finance.tracker.repository;

import com.finance.tracker.domain.transaction.TransactionType;
import com.finance.tracker.entity.FinancialTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialTransactionJpaRepository extends JpaRepository<FinancialTransactionEntity, Long> {


    @Query("Select t from FinancialTransaction t "+
            "Where t.userId = :userId "+
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate "+
            "Order By t.transactionDate DESC")
    List<FinancialTransactionEntity> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    @Query("Select t from FinancialTransaction t "+
            "Where t.userId = :userId "+
            "AND t.status = 'POSTED' "+
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate "+
            "Order By t.transactionDate DESC")
    List<FinancialTransactionEntity> findActiveByUserIdAndDateRange(@Param("userId") Long userId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    @Query("Select t from FinancialTransaction t "+
            "Where t.userId = :userId "+
            "AND t.type = :type "+
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate "+
            "Order By t.transactionDate DESC")
    List<FinancialTransactionEntity> findByUserIdAndTypeAndDateRange(@Param("userId") Long userId,
                                                        @Param("type") TransactionType type,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("Select t from FinancialTransaction t "+
            "Where t.userId = :userId "+
            "AND t.categoryId = :categoryId "+
            "AND t.transactionDate >= :startDate " +
            "AND t.transactionDate <= :endDate "+
            "Order By t.transactionDate DESC")
    List<FinancialTransactionEntity> findByUserIdAndCategoryAndDateRange(@Param("userId") Long userId,
                                                        @Param("categoryId") Long categoryId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);


}
