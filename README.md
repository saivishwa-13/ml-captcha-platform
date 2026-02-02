# ML-Driven CAPTCHA Platform

A modular, ML-driven CAPTCHA replacement platform designed as a SaaS offering for enterprises. This system provides multiple adaptive human-verification challenges using machine learning instead of traditional CAPTCHA systems.

## 🎯 Features

- **Multiple CAPTCHA Types**: Sequence Memory, Pattern & Logic, Arithmetic, Physics-based Mini Games, Behavior Analysis
- **ML-Powered**: CNN, RNN/LSTM, Random Forest, K-Means clustering
- **Enterprise-Ready**: Multi-tenant architecture, subscription management, analytics dashboard
- **Secure**: JWT authentication, rate limiting, replay attack protection
- **Scalable**: Microservices architecture, Docker containerization

## 🏗️ Architecture

See [ARCHITECTURE.md](./ARCHITECTURE.md) for detailed system architecture, database schema, and design decisions.

## 📋 Implementation Plan

See [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) for step-by-step implementation guide.

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Python 3.11+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ml-captcha-platform
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d postgres redis
   ```

3. **Backend Setup**
   ```bash
   cd backend
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **ML Service Setup**
   ```bash
   cd ml-service
   pip install -r requirements.txt
   uvicorn app.main:app --reload
   ```

5. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 📁 Project Structure

```
ml-captcha-platform/
├── backend/          # Spring Boot backend
├── frontend/         # React + TypeScript frontend
├── ml-service/       # FastAPI ML inference service
├── infrastructure/   # Docker, Kubernetes configs
├── ARCHITECTURE.md   # System architecture documentation
├── IMPLEMENTATION_PLAN.md  # Implementation guide
└── README.md        # This file
```

## 🔐 Security

- JWT-based authentication
- Rate limiting per client and IP
- HTTPS/TLS encryption
- Input validation and sanitization
- SQL injection prevention
- XSS protection

## 📊 API Documentation

API documentation will be available at `/api/docs` once the backend is running.

## 🤝 Contributing

This is a private enterprise project. For internal contributions, please follow the coding standards and submit PRs for review.

## 📄 License

Proprietary - All rights reserved

## 📞 Support

For issues or questions, contact the development team.
