CREATE TABLE customers (
    id UUID PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    kyc_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
