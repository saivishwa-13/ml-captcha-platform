-- ML CAPTCHA Platform Database Schema
-- Version 1: Initial schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Clients table
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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
CREATE INDEX idx_clients_active ON clients(is_active);

-- CAPTCHA sessions table
CREATE TABLE captcha_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    challenge_type VARCHAR(50) NOT NULL,
    challenge_data JSONB NOT NULL,
    expected_response JSONB,
    difficulty_level INTEGER DEFAULT 1,
    status VARCHAR(50) DEFAULT 'pending',
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    verification_result BOOLEAN
);

CREATE INDEX idx_sessions_token ON captcha_sessions(session_token);
CREATE INDEX idx_sessions_client ON captcha_sessions(client_id);
CREATE INDEX idx_sessions_status ON captcha_sessions(status);
CREATE INDEX idx_sessions_expires ON captcha_sessions(expires_at);
CREATE INDEX idx_sessions_challenge_type ON captcha_sessions(challenge_type);

-- Behavior metrics table
CREATE TABLE behavior_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES captcha_sessions(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    metric_type VARCHAR(50) NOT NULL,
    metric_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_metrics_session ON behavior_metrics(session_id);
CREATE INDEX idx_metrics_client ON behavior_metrics(client_id);
CREATE INDEX idx_metrics_type ON behavior_metrics(metric_type);
CREATE INDEX idx_metrics_created ON behavior_metrics(created_at);

-- ML predictions table
CREATE TABLE ml_predictions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    session_id UUID NOT NULL REFERENCES captcha_sessions(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    model_type VARCHAR(50) NOT NULL,
    prediction_data JSONB NOT NULL,
    confidence_score DECIMAL(5,4),
    is_human BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_predictions_session ON ml_predictions(session_id);
CREATE INDEX idx_predictions_model ON ml_predictions(model_type);
CREATE INDEX idx_predictions_human ON ml_predictions(is_human);
CREATE INDEX idx_predictions_client ON ml_predictions(client_id);

-- Client analytics table
CREATE TABLE client_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_challenges INTEGER DEFAULT 0,
    successful_verifications INTEGER DEFAULT 0,
    bot_detections INTEGER DEFAULT 0,
    avg_completion_time DECIMAL(10,2),
    challenge_type_distribution JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(client_id, date)
);

CREATE INDEX idx_analytics_client_date ON client_analytics(client_id, date);

-- Rate limits table
CREATE TABLE rate_limits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    ip_address INET,
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rate_limits_client_ip ON rate_limits(client_id, ip_address);
CREATE INDEX idx_rate_limits_window ON rate_limits(window_start, window_end);
CREATE INDEX idx_rate_limits_endpoint ON rate_limits(endpoint);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for clients table
CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
