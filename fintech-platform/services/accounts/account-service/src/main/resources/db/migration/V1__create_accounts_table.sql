CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    ledger_account_id UUID NOT NULL,
    account_number VARCHAR(32) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_accounts_customer_id ON accounts (customer_id);
