package com.finance.tracker.repository;

import com.finance.tracker.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RefreshToken Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByExpiresAtBeforeAndRevokedAtIsNull(java.time.LocalDateTime date);

    void deleteByUserIdAndRevokedAtIsNull(Long userId);
}
