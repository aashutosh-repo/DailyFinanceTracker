package com.finance.tracker.repository;

import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
//    Optional<Budget> findActiveBudgetByUserIdAndCategoryId(String userId, Long categoryId);
    List<Budget> findByUser(User user);
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Budget t
        WHERE t.extUserId = :userId AND t.period=:period AND t.startDate BETWEEN :startDate AND :endDate
    """)
    BigDecimal getMonthlyBudget(@Param("userId") String userId, @Param("period") String period,
                                @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT t FROM Budget t
        WHERE t.extUserId = :userId AND t.period=:period AND t.startDate BETWEEN :startDate AND :endDate
    """)
    List<Budget> getMonthlyBudgetStatus(@Param("userId") String userId, @Param("period") String period,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
