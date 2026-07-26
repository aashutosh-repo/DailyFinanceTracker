package com.finance.tracker.repository;

import com.finance.tracker.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    Page<Income> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT i FROM Income i WHERE i.user.id = :userId " +
            "AND i.incomeDate BETWEEN :startDate AND :endDate " +
            "AND i.deletedAt IS NULL")
    List<Income> findByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
        SELECT COALESCE(SUM(i.amount), 0) FROM Income i
        WHERE i.extUserId = :userId
          AND YEAR(i.incomeDate) = :year
          AND MONTH(i.incomeDate) = :month
    """)
    java.math.BigDecimal sumIncomeByUserAndDateRange(String userId, int year, int month);

    @Query("""
        SELECT COALESCE(SUM(i.amount), 0) FROM Income i
        WHERE i.extUserId = :userId
          AND YEAR(i.incomeDate) = :year
    """)
    java.math.BigDecimal sumIncomeByUserAndYear(String userId, int year);
}
