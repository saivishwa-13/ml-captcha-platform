package com.mlcaptcha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaGenerateResponse {
    private String sessionToken;
    private String challengeType;
    private Map<String, Object> challengeData;
    private LocalDateTime expiresAt;
}
