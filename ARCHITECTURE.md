# ML-Driven CAPTCHA Platform - System Architecture

## 🎯 Overview

A modular, ML-driven CAPTCHA replacement platform designed as a SaaS offering for enterprises. The system provides multiple adaptive human-verification challenges using machine learning instead of traditional CAPTCHA systems.

**Business Model**: ₹2500/month per client subscription
**Target**: Enterprise clients requiring bot protection

---

## 🏗️ High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT WEBSITES                         │
│  (Embedded via SDK/Widget - React Component or API Integration)│
└────────────────────────────┬────────────────────────────────────┘
                              │ HTTPS
                              │ API Calls
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    API GATEWAY / LOAD BALANCER                  │
│              (Rate Limiting, SSL Termination, Routing)           │
└────────────────────────────┬────────────────────────────────────┘
                              │
                ┌─────────────┴─────────────┐
                │                           │
                ▼                           ▼
┌───────────────────────────┐   ┌───────────────────────────┐
│   BACKEND API SERVICE     │   │   ML INFERENCE SERVICE    │
│   (Spring Boot - Java)    │◄──┤   (FastAPI - Python)      │
│                           │   │                           │
│  - REST API Endpoints     │   │  - CNN Models             │
│  - JWT Authentication     │   │  - RNN/LSTM Models        │
│  - CAPTCHA Strategy       │   │  - Random Forest          │
│  - Rate Limiting          │   │  - K-Means Clustering     │
│  - Multi-tenancy          │   │  - Behavior Analysis      │
└───────────┬───────────────┘   └───────────────────────────┘
            │
            │
            ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DATABASE LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ PostgreSQL   │  │ Redis Cache  │  │ TimescaleDB  │         │
│  │ (Primary DB) │  │ (Sessions)   │  │ (Metrics)    │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN DASHBOARD (React)                      │
│  - Client Management                                            │
│  - Analytics & Metrics                                          │
│  - Subscription Management                                      │
│  - Model Performance Monitoring                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🧠 CAPTCHA Challenge Types

### 1. Sequence Memory CAPTCHA
- **Type**: Visual/Numeric/Symbolic sequence recall
- **ML Model**: RNN/LSTM for adaptive sequence generation
- **Difficulty Scaling**: Based on sequence length and complexity
- **Human Behavior**: Natural recall patterns, slight delays

### 2. Pattern & Logic CAPTCHA
- **Type**: Continue the pattern (numbers, shapes, rotations)
- **ML Model**: CNN for visual pattern recognition
- **Difficulty Scaling**: Pattern complexity, rotation angles
- **Human Behavior**: Pattern recognition with reasoning

### 3. Arithmetic CAPTCHA
- **Type**: Time-bound logic/math questions
- **ML Model**: Rule-based with difficulty adaptation
- **Difficulty Scaling**: Based on user success rate
- **Human Behavior**: Variable solving time, natural errors

### 4. Physics-based Mini Game CAPTCHA
- **Type**: Drag, drop, gravity, collision-based puzzles
- **ML Model**: Behavioral analysis of mouse movements
- **Difficulty Scaling**: Puzzle complexity, physics parameters
- **Human Behavior**: Natural motor behavior, acceleration curves

### 5. Clustering-based Behavior CAPTCHA
- **Type**: Mouse movement, keystroke timing analysis
- **ML Model**: K-Means clustering for behavior patterns
- **Difficulty Scaling**: N/A (passive analysis)
- **Human Behavior**: Natural mouse paths, typing rhythms

### 6. Random Forest Classifier (Final Decision Layer)
- **Purpose**: Aggregate all signals to classify Human vs Bot
- **Features**: 
  - Challenge completion time
  - Mouse movement patterns
  - Keystroke dynamics
  - Challenge-specific metrics
  - Historical behavior patterns

---

## 🗄️ Database Schema

### Core Tables

#### `clients`
```sql
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) UNIQUE NOT NULL,
    api_secret VARCHAR(255) NOT NULL,
    subscription_tier VARCHAR(50) DEFAULT 'standard',
    subscription_status VARCHAR(50) DEFAULT 'active',
    subscription_start_date TIMESTAMP,
    subscription_end_date TIMESTAMP,
    monthly_rate DECIMAL(10,2) DEFAULT 2500.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

CREATE INDEX idx_clients_api_key ON clients(api_key);
CREATE INDEX idx_clients_subscription_status ON clients(subscription_status);
```

#### `captcha_sessions`
```sql
CREATE TABLE captcha_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    session_token VARCHAR(255) UNIQUE NOT NULL,
    challenge_type VARCHAR(50) NOT NULL,
    challenge_data JSONB NOT NULL,
    expected_response JSONB,
    difficulty_level INTEGER DEFAULT 1,
    status VARCHAR(50) DEFAULT 'pending', -- pending, completed, failed, expired
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    verification_result BOOLEAN,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_sessions_token ON captcha_sessions(session_token);
CREATE INDEX idx_sessions_client ON captcha_sessions(client_id);
CREATE INDEX idx_sessions_status ON captcha_sessions(status);
CREATE INDEX idx_sessions_expires ON captcha_sessions(expires_at);
```

#### `behavior_metrics`
```sql
CREATE TABLE behavior_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES captcha_sessions(id),
    client_id UUID NOT NULL REFERENCES clients(id),
    metric_type VARCHAR(50) NOT NULL, -- mouse_movement, keystroke, timing, etc.
    metric_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES captcha_sessions(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_metrics_session ON behavior_metrics(session_id);
CREATE INDEX idx_metrics_client ON behavior_metrics(client_id);
CREATE INDEX idx_metrics_type ON behavior_metrics(metric_type);
CREATE INDEX idx_metrics_created ON behavior_metrics(created_at);
```

#### `ml_predictions`
```sql
CREATE TABLE ml_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES captcha_sessions(id),
    client_id UUID NOT NULL REFERENCES clients(id),
    model_type VARCHAR(50) NOT NULL, -- random_forest, kmeans, cnn, lstm
    prediction_data JSONB NOT NULL,
    confidence_score DECIMAL(5,4),
    is_human BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES captcha_sessions(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_predictions_session ON ml_predictions(session_id);
CREATE INDEX idx_predictions_model ON ml_predictions(model_type);
CREATE INDEX idx_predictions_human ON ml_predictions(is_human);
```

#### `client_analytics`
```sql
CREATE TABLE client_analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    date DATE NOT NULL,
    total_challenges INTEGER DEFAULT 0,
    successful_verifications INTEGER DEFAULT 0,
    bot_detections INTEGER DEFAULT 0,
    avg_completion_time DECIMAL(10,2),
    challenge_type_distribution JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id),
    UNIQUE(client_id, date)
);

CREATE INDEX idx_analytics_client_date ON client_analytics(client_id, date);
```

#### `rate_limits`
```sql
CREATE TABLE rate_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES clients(id),
    ip_address INET,
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_rate_limits_client_ip ON rate_limits(client_id, ip_address);
CREATE INDEX idx_rate_limits_window ON rate_limits(window_start, window_end);
```

---

## 🔐 Security Threat Model

### Threat Vectors

1. **Bot Attacks**
   - Automated solving attempts
   - ML model evasion
   - Replay attacks
   - **Mitigation**: Behavioral analysis, rate limiting, challenge rotation

2. **API Abuse**
   - API key theft/leakage
   - DDoS attacks
   - **Mitigation**: JWT authentication, rate limiting, IP whitelisting

3. **Session Hijacking**
   - Token theft
   - Replay attacks
   - **Mitigation**: Short-lived tokens, HTTPS only, token rotation

4. **Model Poisoning**
   - Adversarial inputs
   - Training data manipulation
   - **Mitigation**: Input validation, model versioning, anomaly detection

5. **Data Privacy**
   - Client data exposure
   - Behavioral data leakage
   - **Mitigation**: Encryption at rest, data anonymization, GDPR compliance

### Security Measures

- **Authentication**: JWT with short expiration (15 minutes)
- **Rate Limiting**: Per-client and per-IP limits
- **Encryption**: TLS 1.3 for transit, AES-256 for at-rest
- **Input Validation**: Strict schema validation for all inputs
- **Replay Protection**: Nonce/timestamp validation
- **Audit Logging**: All API calls logged
- **CORS**: Strict origin validation
- **SQL Injection**: Parameterized queries only
- **XSS**: Content Security Policy, input sanitization

---

## 🔄 ML Pipeline Design

### Model Training Pipeline

```
┌─────────────────┐
│  Data Collection│
│  (Behavior Logs)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Data Preprocessing│
│  - Feature Extraction│
│  - Normalization │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Model Training │
│  - CNN (Images) │
│  - RNN/LSTM     │
│  - Random Forest│
│  - K-Means      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Model Evaluation│
│  - Accuracy     │
│  - F1 Score     │
│  - ROC-AUC      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Model Deployment│
│  - Versioning   │
│  - A/B Testing  │
└─────────────────┘
```

### Inference Pipeline

```
Request → Feature Extraction → Model Inference → Aggregation → Decision
```

### Models

1. **CNN** (Visual Pattern Recognition)
   - Input: Image patterns, shapes
   - Output: Pattern classification
   - Framework: TensorFlow/PyTorch

2. **RNN/LSTM** (Sequence Generation)
   - Input: Sequence history
   - Output: Next sequence element
   - Framework: TensorFlow/PyTorch

3. **Random Forest** (Final Classification)
   - Input: Aggregated features from all challenges
   - Output: Human/Bot probability
   - Framework: Scikit-learn

4. **K-Means** (Behavior Clustering)
   - Input: Mouse movements, keystroke timings
   - Output: Behavior cluster assignment
   - Framework: Scikit-learn

---

## 📡 API Design

### Authentication
All endpoints require JWT authentication except `/client/register`.

### Endpoints

#### `POST /api/v1/client/register`
Register a new client
```json
Request:
{
  "companyName": "Example Corp",
  "email": "admin@example.com",
  "subscriptionTier": "standard"
}

Response:
{
  "clientId": "uuid",
  "apiKey": "key",
  "apiSecret": "secret"
}
```

#### `POST /api/v1/captcha/generate`
Generate a new CAPTCHA challenge
```json
Request:
{
  "challengeType": "sequence_memory", // optional, defaults to random
  "difficultyLevel": 1, // optional
  "clientMetadata": {} // optional
}

Response:
{
  "sessionToken": "token",
  "challengeType": "sequence_memory",
  "challengeData": {
    "sequence": [1, 2, 3, 4],
    "displayDuration": 5000
  },
  "expiresAt": "2026-02-02T12:00:00Z"
}
```

#### `POST /api/v1/captcha/verify`
Verify CAPTCHA response
```json
Request:
{
  "sessionToken": "token",
  "response": {
    "answer": [1, 2, 3, 4],
    "behaviorMetrics": {
      "mouseMovements": [...],
      "keystrokeTimings": [...],
      "completionTime": 4500
    }
  }
}

Response:
{
  "verified": true,
  "confidence": 0.95,
  "isHuman": true,
  "token": "verification_token"
}
```

#### `POST /api/v1/metrics/behavior`
Submit behavioral metrics (optional, for analysis)
```json
Request:
{
  "sessionToken": "token",
  "metrics": {
    "mouseMovements": [...],
    "keystrokeTimings": [...],
    "scrollEvents": [...]
  }
}
```

#### `GET /api/v1/analytics/dashboard`
Get client analytics (admin only)
```json
Response:
{
  "totalChallenges": 10000,
  "successRate": 0.92,
  "botDetectionRate": 0.08,
  "avgCompletionTime": 4.5,
  "challengeDistribution": {...}
}
```

---

## 📁 Project Structure

### Backend (Spring Boot)
```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── mlcaptcha/
│       │           ├── MlCaptchaApplication.java
│       │           ├── config/
│       │           │   ├── SecurityConfig.java
│       │           │   ├── RedisConfig.java
│       │           │   └── WebConfig.java
│       │           ├── controller/
│       │           │   ├── CaptchaController.java
│       │           │   ├── ClientController.java
│       │           │   ├── AnalyticsController.java
│       │           │   └── MetricsController.java
│       │           ├── service/
│       │           │   ├── CaptchaService.java
│       │           │   ├── ClientService.java
│       │           │   ├── MLInferenceService.java
│       │           │   ├── RateLimitService.java
│       │           │   └── challenge/
│       │           │       ├── ChallengeStrategy.java
│       │           │       ├── SequenceMemoryStrategy.java
│       │           │       ├── PatternLogicStrategy.java
│       │           │       ├── ArithmeticStrategy.java
│       │           │       ├── PhysicsGameStrategy.java
│       │           │       └── BehaviorAnalysisStrategy.java
│       │           ├── model/
│       │           │   ├── Client.java
│       │           │   ├── CaptchaSession.java
│       │           │   ├── BehaviorMetric.java
│       │           │   ├── MLPrediction.java
│       │           │   └── ClientAnalytics.java
│       │           ├── repository/
│       │           │   ├── ClientRepository.java
│       │           │   ├── CaptchaSessionRepository.java
│       │           │   ├── BehaviorMetricRepository.java
│       │           │   └── MLPredictionRepository.java
│       │           ├── security/
│       │           │   ├── JwtTokenProvider.java
│       │           │   ├── JwtAuthenticationFilter.java
│       │           │   └── RateLimitInterceptor.java
│       │           └── dto/
│       │               ├── CaptchaGenerateRequest.java
│       │               ├── CaptchaVerifyRequest.java
│       │               └── BehaviorMetricsRequest.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/ (Flyway)
│               ├── V1__initial_schema.sql
│               └── V2__add_indexes.sql
├── pom.xml
└── Dockerfile
```

### Frontend (React + TypeScript)
```
frontend/
├── public/
├── src/
│   ├── components/
│   │   ├── captcha/
│   │   │   ├── CaptchaWidget.tsx
│   │   │   ├── SequenceMemory.tsx
│   │   │   ├── PatternLogic.tsx
│   │   │   ├── Arithmetic.tsx
│   │   │   ├── PhysicsGame.tsx
│   │   │   └── BehaviorTracker.tsx
│   │   ├── dashboard/
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Analytics.tsx
│   │   │   ├── ClientManagement.tsx
│   │   │   └── MetricsChart.tsx
│   │   └── common/
│   │       ├── Button.tsx
│   │       ├── Input.tsx
│   │       └── LoadingSpinner.tsx
│   ├── services/
│   │   ├── api.ts
│   │   ├── captcha.ts
│   │   └── analytics.ts
│   ├── hooks/
│   │   ├── useCaptcha.ts
│   │   ├── useBehaviorTracking.ts
│   │   └── useAnalytics.ts
│   ├── utils/
│   │   ├── behaviorTracking.ts
│   │   ├── mouseTracking.ts
│   │   └── keystrokeTracking.ts
│   ├── types/
│   │   ├── captcha.ts
│   │   ├── client.ts
│   │   └── analytics.ts
│   ├── App.tsx
│   └── index.tsx
├── package.json
├── tsconfig.json
└── Dockerfile
```

### ML Service (FastAPI - Python)
```
ml-service/
├── app/
│   ├── __init__.py
│   ├── main.py
│   ├── models/
│   │   ├── __init__.py
│   │   ├── cnn_model.py
│   │   ├── lstm_model.py
│   │   ├── random_forest_model.py
│   │   └── kmeans_clustering.py
│   ├── services/
│   │   ├── __init__.py
│   │   ├── inference.py
│   │   ├── feature_extraction.py
│   │   └── behavior_analysis.py
│   ├── api/
│   │   ├── __init__.py
│   │   ├── routes.py
│   │   └── schemas.py
│   └── utils/
│       ├── __init__.py
│       └── preprocessing.py
├── models/ (trained models)
│   ├── cnn/
│   ├── lstm/
│   ├── random_forest/
│   └── kmeans/
├── requirements.txt
├── Dockerfile
└── train.py (training scripts)
```

### Infrastructure
```
infrastructure/
├── docker-compose.yml
├── nginx/
│   └── nginx.conf
└── kubernetes/ (optional)
    ├── backend-deployment.yaml
    ├── frontend-deployment.yaml
    └── ml-service-deployment.yaml
```

---

## 🚀 Implementation Plan

### Phase 1: Foundation (Week 1-2)
1. Set up project structure
2. Database schema implementation
3. Basic Spring Boot backend with authentication
4. Basic React frontend setup
5. Docker containerization

### Phase 2: Core CAPTCHA Engine (Week 3-4)
1. Implement Challenge Strategy pattern
2. Sequence Memory CAPTCHA
3. Pattern & Logic CAPTCHA
4. Arithmetic CAPTCHA
5. Basic ML inference service setup

### Phase 3: Advanced CAPTCHA Types (Week 5-6)
1. Physics-based Mini Game CAPTCHA
2. Behavior tracking implementation
3. K-Means clustering integration
4. Random Forest classifier integration

### Phase 4: ML Pipeline (Week 7-8)
1. Model training pipeline
2. Feature extraction
3. Model deployment
4. A/B testing framework

### Phase 5: SaaS Features (Week 9-10)
1. Multi-tenancy implementation
2. Subscription management
3. Analytics dashboard
4. Client management UI

### Phase 6: Security & Optimization (Week 11-12)
1. Security hardening
2. Rate limiting
3. Performance optimization
4. Load testing
5. Documentation

---

## 📊 Technology Stack

### Backend
- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7+
- **ORM**: JPA/Hibernate
- **Migration**: Flyway
- **Security**: Spring Security + JWT

### Frontend
- **Framework**: React 18+
- **Language**: TypeScript 5+
- **State Management**: React Query / Zustand
- **UI Library**: Material-UI / Tailwind CSS
- **Canvas**: Fabric.js / Konva.js
- **Physics**: Matter.js

### ML Service
- **Framework**: FastAPI
- **Language**: Python 3.11+
- **ML Libraries**: TensorFlow, PyTorch, Scikit-learn
- **Data Processing**: NumPy, Pandas

### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Docker Compose / Kubernetes
- **Reverse Proxy**: Nginx
- **Monitoring**: Prometheus + Grafana (optional)

---

## 📝 Next Steps

1. Review and approve architecture
2. Set up development environment
3. Initialize Git repository
4. Create database schema
5. Implement Phase 1 components
6. Iterate based on feedback
