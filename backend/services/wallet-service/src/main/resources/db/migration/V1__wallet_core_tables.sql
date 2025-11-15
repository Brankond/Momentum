CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    external_id VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    currency CHAR(3) NOT NULL,
    balance_minor_units BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_wallet_user_currency UNIQUE (user_id, currency)
);

CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallets (user_id);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL REFERENCES wallets (id) ON UPDATE CASCADE ON DELETE RESTRICT,
    type VARCHAR(24) NOT NULL,
    amount_minor_units BIGINT NOT NULL,
    running_balance_minor_units BIGINT NOT NULL,
    reference VARCHAR(120) NOT NULL,
    description TEXT,
    metadata JSONB,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_ledger_wallet_reference UNIQUE (wallet_id, reference)
);

CREATE INDEX IF NOT EXISTS idx_ledger_wallet_date ON ledger_entries (wallet_id, occurred_at);
