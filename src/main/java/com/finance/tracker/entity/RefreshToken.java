package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Refresh Token Entity
 * Stores refresh tokens for JWT authentication with expiration and revocation support
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_token_hash", columnList = "token_hash"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;
    
    @Column(nullable = false, length = 64)
    private String tokenHash;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column
    private LocalDateTime revokedAt;
    
    /**
     * Check if token is expired
     */
    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if token is revoked
     */
    @Transient
    public boolean isRevoked() {
        return revokedAt != null;
    }
    
    /**
     * Check if token is valid (not expired and not revoked)
     */
    @Transient
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }
}
