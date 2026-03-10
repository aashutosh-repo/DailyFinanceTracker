package com.finance.tracker.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * AuditLog Repository
 */
//@Repository
//public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
//    @Query("SELECT al FROM AuditLog al WHERE al.entityType = :entityType " +
//            "AND al.entityId = :entityId ORDER BY al.createdAt DESC")
//    Page<AuditLog> findByEntity(String entityType, Long entityId, Pageable pageable);
//}
