package com.finance.tracker.repository;

import com.finance.tracker.entity.BudgetAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetAlertRepository extends JpaRepository<BudgetAlert, Long> {
    Page<BudgetAlert> findByBudgetUserIdAndIsAcknowledgedFalse(Long userId, Pageable pageable);
    List<BudgetAlert> findByBudgetIdAndIsAcknowledgedFalse(Long budgetId);
}
