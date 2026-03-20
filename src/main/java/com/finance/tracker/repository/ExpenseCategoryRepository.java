package com.finance.tracker.repository;

import com.finance.tracker.entity.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    Page<ExpenseCategory> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<ExpenseCategory> findByUserIdAndNameAndDeletedAtIsNull(Long userId, String name);
    boolean existsByUserIdAndNameAndDeletedAtIsNull(Long userId, String name);
    Optional<ExpenseCategory> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
