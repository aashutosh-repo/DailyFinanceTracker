package com.finance.tracker.repository;

import com.finance.tracker.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND CURRENT_DATE BETWEEN b.startDate AND b.endDate " +
            "AND b.isActive = true " +
            "AND b.deletedAt IS NULL")
    List<Budget> findActiveBudgetsForUser(Long userId);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
            "AND b.category.id = :categoryId " +
            "AND CURRENT_DATE BETWEEN b.startDate AND b.endDate " +
            "AND b.deletedAt IS NULL")
    Optional<Budget> findActiveBudgetByUserAndCategory(Long userId, Long categoryId);
}
