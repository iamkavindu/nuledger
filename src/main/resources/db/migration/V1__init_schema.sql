-- =============================================================================
-- NuLedger V1: core schema, RLS, append-only journal
-- Applied by Flyway as nuledger_owner
-- =============================================================================
-- -----------------------------------------------------------------------------
-- 1. Session helper for RLS (used later from app via SET LOCAL)
-- -----------------------------------------------------------------------------
-- App will run: SET LOCAL app.tenant_id = 'tenant-uuid';
-- -----------------------------------------------------------------------------
-- 2. Tables
-- -----------------------------------------------------------------------------
CREATE TABLE accounts
(
    id             UUID PRIMARY KEY      DEFAULT uuidv7(),
    tenant_id      VARCHAR(64)  NOT NULL,
    code           VARCHAR(64)  NOT NULL,
    name           VARCHAR(255) NOT NULL,
    account_type   VARCHAR(32)  NOT NULL,
    allow_negative BOOLEAN      NOT NULL DEFAULT FALSE,
    status         VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT account_type_chk CHECK ( account_type IN ('ASSET', 'LIABILITY', 'REVENUE', 'EXPENSE') ),
    CONSTRAINT account_status_chk CHECK ( status IN ('ACTIVE', 'INACTIVE', 'CLOSED') ),
    CONSTRAINT uq_accounts_tenant_code UNIQUE (tenant_id, code)
);

CREATE TABLE journal_entries
(
    id                UUID PRIMARY KEY      DEFAULT uuidv7(),
    tenant_id         VARCHAR(64)  NOT NULL,
    idempotency_key   VARCHAR(128) NOT NULL,
    status            VARCHAR(32)  NOT NULL DEFAULT 'POSTED',
    reverses_entry_id UUID REFERENCES journal_entries (id),
    correlation_id    VARCHAR(128),
    recorded_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    metadata          JSONB,
    CONSTRAINT journal_entries_status_chk CHECK (status IN ('POSTED', 'REVERSED')),
    CONSTRAINT uq_journal_entries_tenant_idempotency UNIQUE (tenant_id, idempotency_key)
);

CREATE TABLE journal_lines
(
    id         UUID PRIMARY KEY DEFAULT uuidv7(),
    tenant_id  VARCHAR(64)    NOT NULL,
    entry_id   UUID           NOT NULL REFERENCES journal_entries (id),
    account_id UUID           NOT NULL REFERENCES accounts (id),
    direction  VARCHAR(8)     NOT NULL,
    amount     NUMERIC(19, 4) NOT NULL,
    currency   CHAR(3)        NOT NULL,
    line_order INT            NOT NULL,
    CONSTRAINT journal_lines_direction_chk CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT journal_lines_amount_chk CHECK (amount > 0),
    CONSTRAINT uq_journal_lines_entry_order UNIQUE (entry_id, line_order)
);

CREATE TABLE account_balances
(
    account_id UUID           NOT NULL REFERENCES accounts (id),
    tenant_id  VARCHAR(64)    NOT NULL,
    currency   CHAR(3)        NOT NULL,
    balance    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, currency)
);

-- -----------------------------------------------------------------------------
-- 3. Indexes (tenant-scoped queries)
-- -----------------------------------------------------------------------------
CREATE INDEX idx_accounts_tenant ON accounts (tenant_id);
CREATE INDEX idx_journal_entries_tenant_recorded ON journal_entries (tenant_id, recorded_at DESC);
CREATE INDEX idx_journal_entries_tenant_correlation ON journal_entries (tenant_id, correlation_id);
CREATE INDEX idx_journal_lines_tenant_account ON journal_lines (tenant_id, account_id);
CREATE INDEX idx_journal_lines_entry ON journal_lines (entry_id);

-- -----------------------------------------------------------------------------
-- 5. Append-only triggers (journal tables)
-- -----------------------------------------------------------------------------
CREATE
OR REPLACE FUNCTION forbid_journal_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE
EXCEPTION 'Journal records are append-only: % on % is forbidden', TG_OP, TG_TABLE_NAME;
END;
$$;
CREATE TRIGGER trg_journal_entries_immutable
    BEFORE UPDATE OR
DELETE
ON journal_entries
    FOR EACH ROW
    EXECUTE FUNCTION forbid_journal_mutation();
CREATE TRIGGER trg_journal_lines_immutable
    BEFORE UPDATE OR
DELETE
ON journal_lines
    FOR EACH ROW
    EXECUTE FUNCTION forbid_journal_mutation();

-- -----------------------------------------------------------------------------
-- 6. Row Level Security
-- -----------------------------------------------------------------------------
ALTER TABLE accounts ENABLE ROW LEVEL SECURITY;
ALTER TABLE journal_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE journal_lines ENABLE ROW LEVEL SECURITY;
ALTER TABLE account_balances ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts FORCE ROW LEVEL SECURITY;
ALTER TABLE journal_entries FORCE ROW LEVEL SECURITY;
ALTER TABLE journal_lines FORCE ROW LEVEL SECURITY;
ALTER TABLE account_balances FORCE ROW LEVEL SECURITY;
CREATE
POLICY tenant_isolation_accounts ON accounts
    USING (tenant_id = current_setting('app.tenant_id', true))
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true));
CREATE
POLICY tenant_isolation_journal_entries ON journal_entries
    USING (tenant_id = current_setting('app.tenant_id', true))
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true));
CREATE
POLICY tenant_isolation_journal_lines ON journal_lines
    USING (tenant_id = current_setting('app.tenant_id', true))
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true));
CREATE
POLICY tenant_isolation_account_balances ON account_balances
    USING (tenant_id = current_setting('app.tenant_id', true))
    WITH CHECK (tenant_id = current_setting('app.tenant_id', true));

-- -----------------------------------------------------------------------------
-- 7. Grants for application role (principle of least privilege)
-- -----------------------------------------------------------------------------
GRANT USAGE ON SCHEMA
public TO nuledger_app;
-- accounts: metadata can change; journal: insert-only
GRANT SELECT, INSERT, UPDATE ON accounts TO nuledger_app;
GRANT
SELECT,
INSERT
ON journal_entries, journal_lines TO nuledger_app;
GRANT SELECT, INSERT, UPDATE ON account_balances TO nuledger_app;
GRANT
USAGE,
SELECT
ON ALL SEQUENCES IN SCHEMA public TO nuledger_app;
-- Future tables created by owner auto-grant to app
ALTER
DEFAULT PRIVILEGES FOR ROLE nuledger_owner IN SCHEMA public
    GRANT
SELECT,
INSERT
,
UPDATE
ON TABLES TO nuledger_app;
