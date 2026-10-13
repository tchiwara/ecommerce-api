ALTER TABLE orders
    ADD COLUMN stripe_session_id VARCHAR(255) NULL,
    ADD COLUMN stripe_payment_intent_id VARCHAR(255) NULL,
    ADD COLUMN paid_at TIMESTAMP NULL,
    ADD CONSTRAINT uq_orders_stripe_session_id UNIQUE (stripe_session_id);