package com.mlcaptcha.controller;

import com.mlcaptcha.model.Client;
import com.mlcaptcha.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client")
@RequiredArgsConstructor
@Slf4j
public class ClientController {
    
    private final ClientRepository clientRepository;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerClient(@RequestBody Map<String, String> request) {
        String companyName = request.get("companyName");
        String subscriptionTier = request.getOrDefault("subscriptionTier", "standard");
        
        // Generate API credentials
        String apiKey = "mlc_" + UUID.randomUUID().toString().replace("-", "");
        String apiSecret = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        
        // Create client
        Client client = Client.builder()
                .companyName(companyName)
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .subscriptionTier(subscriptionTier)
                .subscriptionStatus("active")
                .subscriptionStartDate(LocalDateTime.now())
                .subscriptionEndDate(LocalDateTime.now().plusMonths(1))
                .monthlyRate(java.math.BigDecimal.valueOf(2500.00))
                .isActive(true)
                .build();
        
        clientRepository.save(client);
        
        Map<String, String> response = new HashMap<>();
        response.put("clientId", client.getId().toString());
        response.put("apiKey", apiKey);
        response.put("apiSecret", apiSecret);
        response.put("message", "Client registered successfully");
        
        return ResponseEntity.ok(response);
    }
}
