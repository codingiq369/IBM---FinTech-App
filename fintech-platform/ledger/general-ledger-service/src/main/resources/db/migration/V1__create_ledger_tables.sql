CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY,
    owner_reference UUID NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE journal_entries (
    id UUID PRIMARY KEY,
    description VARCHAR(500) NOT NULL,
    transaction_reference VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE postings (
    id UUID PRIMARY KEY,
    journal_entry_id UUID NOT NULL REFERENCES journal_entries (id),
    ledger_account_id UUID NOT NULL REFERENCES ledger_accounts (id),
    direction VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_postings_ledger_account_id ON postings (ledger_account_id);
CREATE INDEX idx_postings_journal_entry_id ON postings (journal_entry_id);
