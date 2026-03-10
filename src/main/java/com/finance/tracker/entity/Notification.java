package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Notification Entity
 * Stores user notifications for alerts, reminders, and updates
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_user_id", columnList = "user_id"),
    @Index(name = "idx_notifications_user_id_created_at", columnList = "user_id,created_at"),
    @Index(name = "idx_notifications_user_id_is_read", columnList = "user_id,is_read")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, length = 50)
    private String type; // EXPENSE_CREATED, BUDGET_EXCEEDED, BUDGET_ALERT, INCOME_ADDED, etc.
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column
    private String relatedEntityId; // ID of related expense, budget, etc.
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /**
     * Mark notification as read
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
