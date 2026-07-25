package com.finance.tracker.repository;

import com.finance.tracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
            "AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "AND e.deletedAt IS NULL")
    List<Expense> findByUserAndDateRange(String userId, LocalDate startDate, LocalDate endDate);

//    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
//            "AND e.category.id = :categoryId " +
//            "AND e.deletedAt IS NULL")
//    Page<Expense> findByUserAndCategory(Long userId, Long categoryId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.expenseDate BETWEEN :startDate AND :endDate " +
            "AND e.deletedAt IS NULL")
    java.math.BigDecimal sumExpensesByUserAndDateRange(String userId, LocalDate startDate, LocalDate endDate);
    
//    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
//            "WHERE e.user.id = :userId " +
//            "AND e.category.id = :categoryId " +
//            "AND e.expenseDate BETWEEN :startDate AND :endDate " +
//            "AND e.deletedAt IS NULL")
//    java.math.BigDecimal sumExpensesByCategoryAndDateRange(Long userId, String categoryId, LocalDate startDate, LocalDate endDate);
}