package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Base entity class with common audit fields.
 * All entities should extend this class for consistency.
 */
@MappedSuperclass
@Data
@RequiredArgsConstructor
public abstract class BaseEntity {
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * @return true if entity is soft-deleted
     */
    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    /**
     * Soft delete the entity
     */
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.updatedBy = deletedBy;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
