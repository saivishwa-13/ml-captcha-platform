package com.mlcaptcha.service;

import com.mlcaptcha.dto.CaptchaGenerateResponse;
import com.mlcaptcha.dto.CaptchaVerifyRequest;
import com.mlcaptcha.dto.CaptchaVerifyResponse;
import com.mlcaptcha.service.challenge.ChallengeFactory;
import com.mlcaptcha.service.challenge.ChallengeStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * CAPTCHA Service with in-memory storage.
 * Supports Sequence Memory CAPTCHA only.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaService {
    
    private final ChallengeFactory challengeFactory;
    private final InMemorySessionStorage sessionStorage;
    
    public CaptchaGenerateResponse generateChallenge(String apiKey, String challengeType, int difficultyLevel) {
        // Simple API key validation (accept any non-empty key for now)
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API key is required");
        }
        
        // Force sequence_memory challenge type
        String effectiveChallengeType = "sequence_memory";
        ChallengeStrategy strategy = challengeFactory.getStrategy(effectiveChallengeType);
        
        if (strategy == null) {
            throw new RuntimeException("Sequence Memory challenge strategy not available");
        }
        
        // Generate challenge
        Map<String, Object> challengeData = strategy.generateChallenge(difficultyLevel);
        Map<String, Object> expectedResponse = strategy.calculateExpectedResponse(challengeData);
        
        // Create session
        String sessionToken = UUID.randomUUID().toString();
        InMemorySessionStorage.CaptchaSession session = new InMemorySessionStorage.CaptchaSession();
        session.setSessionToken(sessionToken);
        session.setChallengeType(effectiveChallengeType);
        session.setChallengeData(challengeData);
        session.setExpectedResponse(expectedResponse);
        session.setDifficultyLevel(difficultyLevel);
        session.setStatus("pending");
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        sessionStorage.save(session);
        
        log.info("Generated CAPTCHA challenge: sessionToken={}, challengeType={}, difficulty={}", 
                sessionToken, effectiveChallengeType, difficultyLevel);
        
        return CaptchaGenerateResponse.builder()
                .sessionToken(sessionToken)
                .challengeType(effectiveChallengeType)
                .challengeData(challengeData)
                .expiresAt(session.getExpiresAt())
                .build();
    }
    
    public CaptchaVerifyResponse verifyChallenge(
            String apiKey,
            String sessionToken,
            Map<String, Object> userResponse,
            CaptchaVerifyRequest.BehaviorMetricsRequest behaviorMetrics) {
        
        // Simple API key validation
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API key is required");
        }
        
        // Get session
        InMemorySessionStorage.CaptchaSession session = sessionStorage.findBySessionToken(sessionToken);
        if (session == null) {
            throw new RuntimeException("Invalid session token");
        }
        
        // Check if session is expired
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus("expired");
            sessionStorage.save(session);
            throw new RuntimeException("Session expired");
        }
        
        // Check if already completed
        if (!"pending".equals(session.getStatus())) {
            throw new RuntimeException("Session already processed");
        }
        
        // Verify challenge
        ChallengeStrategy strategy = challengeFactory.getStrategy(session.getChallengeType());
        boolean challengeVerified = strategy.verifyChallenge(session.getExpectedResponse(), userResponse);
        
        // Simple behavior analysis (without ML service dependency)
        boolean isHuman = true; // Default to human for now
        double confidence = 0.95; // Default confidence
        
        if (behaviorMetrics != null) {
            // Basic heuristics
            Long completionTime = behaviorMetrics.getCompletionTime();
            if (completionTime != null && completionTime < 500) {
                isHuman = false;
                confidence = 0.3;
            } else if (completionTime != null && completionTime > 1000) {
                isHuman = true;
                confidence = 0.9;
            }
        }
        
        // Final decision
        boolean verified = challengeVerified && isHuman;
        
        // Update session
        session.setStatus(verified ? "completed" : "failed");
        session.setCompletedAt(LocalDateTime.now());
        session.setVerificationResult(verified);
        sessionStorage.save(session);
        
        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        
        log.info("Verified CAPTCHA: sessionToken={}, verified={}, isHuman={}, confidence={}", 
                sessionToken, verified, isHuman, confidence);
        
        return CaptchaVerifyResponse.builder()
                .verified(verified)
                .confidence(confidence)
                .isHuman(isHuman)
                .token(verificationToken)
                .build();
    }
}
