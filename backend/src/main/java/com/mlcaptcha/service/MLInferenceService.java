package com.mlcaptcha.service;

import com.mlcaptcha.dto.CaptchaVerifyRequest;
import com.mlcaptcha.model.CaptchaSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for ML inference calls to Python ML service.
 * Falls back to rule-based analysis if ML service is unavailable.
 */
@Service
@Slf4j
public class MLInferenceService {
    
    private final WebClient webClient;
    private double lastConfidenceScore = 0.95; // Default confidence
    
    @Value("${ml-service.url:http://localhost:8000}")
    private String mlServiceUrl;
    
    public MLInferenceService() {
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }
    
    public boolean analyzeBehavior(
            CaptchaSession session,
            Map<String, Object> userResponse,
            CaptchaVerifyRequest.BehaviorMetricsRequest behaviorMetrics) {
        
        try {
            // Prepare ML inference request
            Map<String, Object> inferenceRequest = new HashMap<>();
            inferenceRequest.put("session_id", session.getId().toString());
            inferenceRequest.put("challenge_type", session.getChallengeType());
            inferenceRequest.put("user_response", userResponse);
            
            if (behaviorMetrics != null) {
                inferenceRequest.put("mouse_movements", behaviorMetrics.getMouseMovements());
                inferenceRequest.put("keystroke_timings", behaviorMetrics.getKeystrokeTimings());
                inferenceRequest.put("completion_time", behaviorMetrics.getCompletionTime());
            }
            
            // Call ML service
            Map<String, Object> mlResponse = webClient.post()
                    .uri("/api/v1/ml/analyze")
                    .bodyValue(inferenceRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn(fallbackAnalysis(session, userResponse, behaviorMetrics))
                    .block();
            
            if (mlResponse != null) {
                Boolean isHuman = (Boolean) mlResponse.get("is_human");
                Object confidenceObj = mlResponse.get("confidence");
                
                if (confidenceObj != null) {
                    lastConfidenceScore = ((Number) confidenceObj).doubleValue();
                }
                
                return isHuman != null ? isHuman : true; // Default to human if unclear
            }
            
        } catch (Exception e) {
            log.warn("ML service unavailable, using fallback analysis: {}", e.getMessage());
        }
        
        // Fallback to rule-based analysis
        return fallbackRuleBasedAnalysis(session, userResponse, behaviorMetrics);
    }
    
    public double getConfidenceScore() {
        return lastConfidenceScore;
    }
    
    private Map<String, Object> fallbackAnalysis(
            CaptchaSession session,
            Map<String, Object> userResponse,
            CaptchaVerifyRequest.BehaviorMetricsRequest behaviorMetrics) {
        
        boolean isHuman = fallbackRuleBasedAnalysis(session, userResponse, behaviorMetrics);
        
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("is_human", isHuman);
        fallback.put("confidence", isHuman ? 0.85 : 0.60);
        
        return fallback;
    }
    
    private boolean fallbackRuleBasedAnalysis(
            CaptchaSession session,
            Map<String, Object> userResponse,
            CaptchaVerifyRequest.BehaviorMetricsRequest behaviorMetrics) {
        
        // Basic heuristics when ML service is unavailable
        if (behaviorMetrics == null) {
            return true; // Default to human if no behavior data
        }
        
        // Check completion time (too fast = bot, too slow = human)
        Long completionTime = behaviorMetrics.getCompletionTime();
        if (completionTime != null) {
            if (completionTime < 500) {
                return false; // Suspiciously fast
            }
            if (completionTime > 60000) {
                return true; // Human-like delay
            }
        }
        
        // Check mouse movements
        if (behaviorMetrics.getMouseMovements() != null) {
            int movementCount = behaviorMetrics.getMouseMovements().size();
            if (movementCount == 0) {
                return false; // No mouse movement = bot
            }
            if (movementCount > 5) {
                return true; // Natural mouse movement
            }
        }
        
        // Default to human
        return true;
    }
}
