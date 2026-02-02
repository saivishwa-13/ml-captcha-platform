package com.mlcaptcha.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "captcha_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "session_token", unique = true, nullable = false)
    private String sessionToken;

    @Column(name = "challenge_type", nullable = false)
    private String challengeType;

    @Column(name = "challenge_data", nullable = false, columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> challengeData;

    @Column(name = "expected_response", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> expectedResponse;

    @Column(name = "difficulty_level")
    private Integer difficultyLevel;

    @Column(name = "status")
    private String status;

    @Column(name = "ip_address")
    private InetAddress ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "verification_result")
    private Boolean verificationResult;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
