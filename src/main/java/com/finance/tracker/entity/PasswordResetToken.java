package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Password Reset Token Entity
 * Stores one-time tokens for password reset functionality
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_password_reset_tokens_token_hash", columnList = "token_hash"),
    @Index(name = "idx_password_reset_tokens_expires_at", columnList = "expires_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;
    
    @Column(nullable = false, length = 64)
    private String tokenHash;
    
    @Column(nullable = false)
    private String userEmail;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Check if token is expired
     */
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if token is valid (not expired)
     */
    @Transient
    public boolean isValid() {
        return !isExpired();
    }
}
