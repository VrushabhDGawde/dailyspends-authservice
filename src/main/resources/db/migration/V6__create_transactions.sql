CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sender VARCHAR(255),
    sms_body TEXT,
    received_at TIMESTAMP WITH TIME ZONE,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    payment_mode VARCHAR(50),
    account_ref VARCHAR(100),
    merchant_raw VARCHAR(255),
    merchant_clean VARCHAR(255),
    category VARCHAR(100),
    post_balance DECIMAL(15, 2),
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    counterparty_type VARCHAR(50),
    upi_ref VARCHAR(100),
    is_reviewed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
