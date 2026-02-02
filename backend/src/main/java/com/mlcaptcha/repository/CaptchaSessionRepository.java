package com.mlcaptcha.repository;

import com.mlcaptcha.model.CaptchaSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CaptchaSessionRepository extends JpaRepository<CaptchaSession, UUID> {
    Optional<CaptchaSession> findBySessionToken(String sessionToken);
    
    @Modifying
    @Query("UPDATE CaptchaSession cs SET cs.status = 'expired' WHERE cs.expiresAt < :now AND cs.status = 'pending'")
    int expireOldSessions(LocalDateTime now);
    
    long countByClientIdAndStatus(UUID clientId, String status);
}
