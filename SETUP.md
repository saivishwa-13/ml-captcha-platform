# Setup Guide

## Prerequisites

- Java 17+
- Node.js 18+
- Python 3.11+
- PostgreSQL 15+
- Redis 7+
- Docker & Docker Compose (optional)

## Quick Start with Docker

1. **Start infrastructure services**
   ```bash
   cd infrastructure
   docker-compose up -d
   ```

2. **Backend Setup**
   ```bash
   cd backend
   # Update application.yml with database credentials if needed
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
   Backend will be available at `http://localhost:8080`

3. **ML Service Setup**
   ```bash
   cd ml-service
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   uvicorn app.main:app --reload
   ```
   ML service will be available at `http://localhost:8000`

4. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend will be available at `http://localhost:3000`

## Manual Database Setup

1. Create PostgreSQL database:
   ```sql
   CREATE DATABASE mlcaptcha;
   CREATE USER mlcaptcha WITH PASSWORD 'mlcaptcha123';
   GRANT ALL PRIVILEGES ON DATABASE mlcaptcha TO mlcaptcha;
   ```

2. Flyway will automatically run migrations on backend startup.

## Register a Client

```bash
curl -X POST http://localhost:8080/api/v1/client/register \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Test Company",
    "subscriptionTier": "standard"
  }'
```

Save the returned `apiKey` for API calls.

## Test CAPTCHA Generation

```bash
curl -X POST http://localhost:8080/api/v1/captcha/generate \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "challengeType": "sequence_memory",
    "difficultyLevel": 1
  }'
```

## Environment Variables

### Backend (`application.yml`)
- `DB_USERNAME`: PostgreSQL username (default: mlcaptcha)
- `DB_PASSWORD`: PostgreSQL password (default: mlcaptcha123)
- `REDIS_HOST`: Redis host (default: localhost)
- `JWT_SECRET`: JWT secret key (change in production!)

### ML Service
- Set via environment variables or update `app/main.py`

### Frontend
- `VITE_API_URL`: Backend API URL (default: http://localhost:8080/api/v1)

## Troubleshooting

1. **Database connection errors**: Ensure PostgreSQL is running and credentials match
2. **Redis connection errors**: Ensure Redis is running
3. **ML service errors**: Check Python dependencies are installed
4. **Port conflicts**: Change ports in respective config files

## Next Steps

1. Train ML models (see `ml-service/` directory)
2. Configure production settings
3. Set up monitoring and logging
4. Deploy to production environment
