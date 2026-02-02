# Sequence Memory CAPTCHA - Backend Implementation

## Overview

Backend-only implementation of Sequence Memory CAPTCHA with in-memory storage. No database required.

## Changes Made

### 1. In-Memory Session Storage
- Created `InMemorySessionStorage` service to store CAPTCHA sessions in memory
- Uses `ConcurrentHashMap` for thread-safe storage
- Sessions expire after 10 minutes

### 2. Simplified Challenge Strategy Interface
- Updated `ChallengeStrategy` interface to work without database models
- `verifyChallenge()` now takes `expectedResponse` and `userResponse` directly

### 3. Updated CaptchaService
- Removed database dependencies (ClientRepository, CaptchaSessionRepository)
- Removed ML service dependency
- Uses in-memory session storage
- Forces `sequence_memory` challenge type
- Simple behavior analysis (completion time check)

### 4. Simplified Security
- `ApiKeyAuthenticationFilter` accepts any non-empty API key
- No database validation required

### 5. Application Configuration
- Disabled database auto-configuration
- Disabled JPA/Hibernate
- Disabled Flyway migrations
- Application runs without PostgreSQL/Redis

## API Endpoints

### POST /api/v1/captcha/generate

Generate a new Sequence Memory CAPTCHA challenge.

**Headers:**
```
X-API-Key: <any-non-empty-key>
Content-Type: application/json
```

**Request Body:**
```json
{
  "challengeType": "sequence_memory",  // Optional, will be forced to sequence_memory
  "difficultyLevel": 1                 // Optional, defaults to 1 (1-5)
}
```

**Response:**
```json
{
  "sessionToken": "uuid-token",
  "challengeType": "sequence_memory",
  "challengeData": {
    "sequence": [3, 7, 2, 9],
    "sequenceType": "numeric",
    "displayDuration": 4000,
    "length": 4
  },
  "expiresAt": "2026-02-02T12:10:00"
}
```

### POST /api/v1/captcha/verify

Verify a Sequence Memory CAPTCHA response.

**Headers:**
```
X-API-Key: <any-non-empty-key>
Content-Type: application/json
```

**Request Body:**
```json
{
  "sessionToken": "uuid-token-from-generate",
  "response": {
    "sequence": [3, 7, 2, 9]
  },
  "behaviorMetrics": {
    "mouseMovements": [...],
    "keystrokeTimings": [...],
    "completionTime": 4500
  }
}
```

**Response:**
```json
{
  "verified": true,
  "confidence": 0.95,
  "isHuman": true,
  "token": "verification-token-uuid"
}
```

## Example Usage

### Generate Challenge
```bash
curl -X POST http://localhost:8080/api/v1/captcha/generate \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-key-123" \
  -d '{
    "difficultyLevel": 1
  }'
```

### Verify Challenge
```bash
curl -X POST http://localhost:8080/api/v1/captcha/verify \
  -H "Content-Type: application/json" \
  -H "X-API-Key: test-key-123" \
  -d '{
    "sessionToken": "session-token-from-generate",
    "response": {
      "sequence": [3, 7, 2, 9]
    },
    "behaviorMetrics": {
      "completionTime": 4500
    }
  }'
```

## Sequence Memory Challenge Details

### Difficulty Levels
- **Level 1**: 4 items, numeric only
- **Level 2**: 5 items, numeric only
- **Level 3**: 6 items, numeric or symbolic
- **Level 4**: 7 items, numeric or symbolic
- **Level 5**: 8 items, numeric/symbolic/alphanumeric

### Sequence Types
- **numeric**: Digits 0-9
- **symbolic**: Special characters (!, @, #, $, %, &, *, +, =, ?)
- **alphanumeric**: Mix of digits and letters A-Z

### Display Duration
- Base: 1000ms per item
- Adjusted by difficulty: -200ms per difficulty level
- Minimum: 3000ms

## Behavior Analysis

Simple heuristics (no ML service):
- Completion time < 500ms → Bot (confidence: 0.3)
- Completion time > 1000ms → Human (confidence: 0.9)
- Default → Human (confidence: 0.95)

## Running the Application

1. **No database setup required!**

2. **Start the backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. **Application will start on:** `http://localhost:8080`

## Notes

- Sessions are stored in memory and will be lost on restart
- Sessions expire after 10 minutes
- API key validation is simplified (any non-empty key accepted)
- Only Sequence Memory CAPTCHA is supported
- No database, Redis, or external services required
