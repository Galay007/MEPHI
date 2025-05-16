CREATE TABLE IF NOT EXISTS users (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN','USER'))
);

CREATE TABLE IF NOT EXISTS otp_config (
    id          SERIAL PRIMARY KEY,
    length      INT NOT NULL CHECK (length > 0),
    ttl_seconds INT NOT NULL CHECK (ttl_seconds > 0)
);

CREATE TABLE IF NOT EXISTS otp_codes (
    id           SERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    operation_id VARCHAR(100),  -- можно привязать к операции/транзакции
    code         VARCHAR(20) NOT NULL,
    status       VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE','USED','EXPIRED')),
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Добавляем дефолтную конфигурации OTP
INSERT INTO otp_config (length, ttl_seconds)
VALUES (4, 60)