package com.mlcaptcha.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage for CAPTCHA sessions.
 * No database required.
 */
@Service
@Slf4j
public class InMemorySessionStorage {
    
    private final Map<String, CaptchaSession> sessions = new ConcurrentHashMap<>();
    
    public void save(CaptchaSession session) {
        sessions.put(session.getSessionToken(), session);
        log.debug("Session saved: {}", session.getSessionToken());
    }
    
    public CaptchaSession findBySessionToken(String sessionToken) {
        return sessions.get(sessionToken);
    }
    
    public void remove(String sessionToken) {
        sessions.remove(sessionToken);
    }
    
    public void cleanupExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        sessions.entrySet().removeIf(entry -> 
            entry.getValue().getExpiresAt().isBefore(now)
        );
    }
    
    @Data
    public static class CaptchaSession {
        private String sessionToken;
        private String challengeType;
        private Map<String, Object> challengeData;
        private Map<String, Object> expectedResponse;
        private Integer difficultyLevel;
        private String status;
        private LocalDateTime expiresAt;
        private LocalDateTime completedAt;
        private Boolean verificationResult;
    }
}
