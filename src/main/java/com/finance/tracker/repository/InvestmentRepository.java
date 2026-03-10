package com.finance.tracker.repository;

import com.finance.tracker.entity.Investment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Investment Repository
 */
@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    Page<Investment> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    @Query("SELECT i FROM Investment i WHERE i.user.id = :userId " +
            "AND i.status = 'ACTIVE' " +
            "AND i.deletedAt IS NULL")
    List<Investment> findActiveInvestmentsByUser(Long userId);
}
