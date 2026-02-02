package com.mlcaptcha.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CaptchaGenerateRequest {
    private String challengeType; // Optional: if not provided, random challenge is selected
    
    @Min(1)
    @Max(5)
    private Integer difficultyLevel; // Optional: defaults to 1
    
    private java.util.Map<String, Object> clientMetadata; // Optional: additional client context
}
