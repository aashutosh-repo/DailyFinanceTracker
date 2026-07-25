package com.finance.tracker.repository;

import com.finance.tracker.entity.Budget;
import com.finance.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
//    Optional<Budget> findActiveBudgetByUserIdAndCategoryId(String userId, Long categoryId);
    List<Budget> findByUser(User user);
}
