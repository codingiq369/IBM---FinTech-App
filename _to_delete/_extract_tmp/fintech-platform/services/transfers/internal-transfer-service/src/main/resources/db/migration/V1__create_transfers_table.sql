CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    source_account_id UUID NOT NULL,
    destination_account_id UUID NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL,
    journal_entry_reference UUID,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transfers_source_account_id ON transfers (source_account_id);
CREATE INDEX idx_transfers_destination_account_id ON transfers (destination_account_id);
