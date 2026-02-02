package com.mlcaptcha.controller;

import com.mlcaptcha.dto.*;
import com.mlcaptcha.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    @PostMapping("/generate")
    public ResponseEntity<CaptchaGenerateResponse> generateCaptcha(
            @Valid @RequestBody CaptchaGenerateRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        CaptchaGenerateResponse response = captchaService.generateChallenge(
                apiKey, 
                request.getChallengeType(), 
                request.getDifficultyLevel() != null ? request.getDifficultyLevel() : 1
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/verify")
    public ResponseEntity<CaptchaVerifyResponse> verifyCaptcha(
            @Valid @RequestBody CaptchaVerifyRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        
        CaptchaVerifyResponse response = captchaService.verifyChallenge(
                apiKey,
                request.getSessionToken(),
                request.getResponse(),
                request.getBehaviorMetrics()
        );
        
        return ResponseEntity.ok(response);
    }
}
