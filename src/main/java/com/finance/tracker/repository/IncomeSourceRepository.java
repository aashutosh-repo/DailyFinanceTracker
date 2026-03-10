package com.finance.tracker.repository;

import com.finance.tracker.entity.IncomeSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {
    Page<IncomeSource> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
    Optional<IncomeSource> findByUserIdAndNameAndDeletedAtIsNull(Long userId, String name);
}