CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100),
    amount NUMERIC(18,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_method VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    provider VARCHAR(32),
    provider_payment_id VARCHAR(128),
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
ALTER TABLE payments ALTER COLUMN payment_method DROP NOT NULL;
CREATE INDEX IF NOT EXISTS idx_payments_provider_payment_id ON payments(provider_payment_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

CREATE TABLE IF NOT EXISTS payment_transaction_events (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    status VARCHAR(32),
    provider VARCHAR(32),
    provider_payment_id VARCHAR(128),
    response_code VARCHAR(64),
    response_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_event_payment FOREIGN KEY (payment_id) REFERENCES payments(payment_id)
);

CREATE INDEX IF NOT EXISTS idx_payment_events_payment_id ON payment_transaction_events(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_events_created_at ON payment_transaction_events(created_at);

CREATE TABLE IF NOT EXISTS payment_transaction_event_details (
    payment_transaction_event_id BIGINT NOT NULL,
    detail_key VARCHAR(100) NOT NULL,
    detail_value VARCHAR(500),
    PRIMARY KEY (payment_transaction_event_id, detail_key),
    CONSTRAINT fk_payment_event_detail_event FOREIGN KEY (payment_transaction_event_id)
        REFERENCES payment_transaction_events(id)
);
