-- The official postgres image only auto-creates the database named in
-- POSTGRES_DB. Each microservice in this slice owns its own database (real
-- microservice data isolation, not just separate schemas), so we create the
-- rest here. This script only runs once, the first time the postgres data
-- volume is initialized.
CREATE DATABASE customer_db;
CREATE DATABASE accounts_db;
CREATE DATABASE ledger_db;
CREATE DATABASE transfers_db;
CREATE DATABASE card_management_db;
CREATE DATABASE card_authorization_db;
