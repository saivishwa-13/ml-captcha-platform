package com.mlcaptcha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVerifyResponse {
    private Boolean verified;
    private Double confidence;
    private Boolean isHuman;
    private String token; // Verification token for client use
}
