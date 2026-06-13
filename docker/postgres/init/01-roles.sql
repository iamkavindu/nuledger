-- Runs once on empty volume as bootstrap user `postgres`

-- Migration / DDL role (owns tables)
CREATE ROLE nuledger_owner
    WITH LOGIN PASSWORD 'nuledger_owner'
    NOSUPERUSER NOBYPASSRLS NOCREATEDB NOCREATEROLE;

-- Application runtime role (RLS enforced)
CREATE ROLE nuledger_app
    WITH LOGIN PASSWORD 'nuledger_app'
    NOSUPERUSER NOBYPASSRLS NOCREATEDB NOCREATEROLE;

ALTER DATABASE nuledger OWNER TO nuledger_owner;

GRANT CONNECT ON DATABASE nuledger TO nuledger_app;
GRANT USAGE, CREATE ON SCHEMA public TO nuledger_owner;
GRANT USAGE ON SCHEMA public TO nuledger_app;