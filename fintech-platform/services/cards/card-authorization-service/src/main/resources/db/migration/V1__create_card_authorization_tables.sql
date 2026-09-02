CREATE TABLE card_authorizations (
    id UUID PRIMARY KEY,
    card_id UUID NOT NULL,
    account_id UUID NOT NULL,
    merchant_name VARCHAR(120) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(10) NOT NULL,
    journal_entry_reference UUID,
    decline_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_card_authorizations_card_id ON card_authorizations (card_id);
CREATE INDEX idx_card_authorizations_card_id_status_created_at
    ON card_authorizations (card_id, status, created_at);

-- One row per currency: the card network's own clearing ledger account,
-- created lazily on first use by ClearingAccountService. See ADR-0010.
CREATE TABLE clearing_accounts (
    currency VARCHAR(3) PRIMARY KEY,
    ledger_account_id UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL
);
