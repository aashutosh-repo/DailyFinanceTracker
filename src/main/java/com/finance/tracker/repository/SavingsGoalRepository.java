package com.finance.tracker.repository;

import com.finance.tracker.entity.SavingsGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    Page<SavingsGoal> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT sg FROM SavingsGoal sg WHERE sg.user.id = :userId " +
            "AND sg.status IN ('ACTIVE', 'PAUSED') " +
            "AND sg.deletedAt IS NULL")
    List<SavingsGoal> findActiveSavingsGoalsByUser(Long userId);
}