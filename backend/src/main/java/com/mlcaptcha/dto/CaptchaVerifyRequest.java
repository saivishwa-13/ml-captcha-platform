package com.mlcaptcha.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class CaptchaVerifyRequest {
    @NotBlank
    private String sessionToken;
    
    private Map<String, Object> response;
    
    private BehaviorMetricsRequest behaviorMetrics;
    
    @Data
    public static class BehaviorMetricsRequest {
        private java.util.List<Map<String, Object>> mouseMovements;
        private java.util.List<Map<String, Object>> keystrokeTimings;
        private Long completionTime; // milliseconds
    }
}
