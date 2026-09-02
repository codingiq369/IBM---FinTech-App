CREATE TABLE cards (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    card_number_masked VARCHAR(19) NOT NULL UNIQUE,
    card_number_last_four VARCHAR(4) NOT NULL,
    cardholder_name VARCHAR(120) NOT NULL,
    card_type VARCHAR(10) NOT NULL,
    expiry_month INT NOT NULL,
    expiry_year INT NOT NULL,
    status VARCHAR(10) NOT NULL,
    daily_purchase_limit NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    activated_at TIMESTAMPTZ,
    blocked_at TIMESTAMPTZ
);

CREATE INDEX idx_cards_account_id ON cards (account_id);
CREATE INDEX idx_cards_customer_id ON cards (customer_id);
