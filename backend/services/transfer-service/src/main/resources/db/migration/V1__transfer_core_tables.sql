CREATE TABLE IF NOT EXISTS transfers (
    transfer_id UUID PRIMARY KEY,
    source_wallet_id UUID NOT NULL,
    destination_wallet_id UUID NOT NULL,
    amount_minor_units BIGINT NOT NULL CHECK (amount_minor_units > 0),
    currency CHAR(3) NOT NULL,
    reference VARCHAR(120) NOT NULL,
    description TEXT,
    metadata JSONB,
    correlation_id UUID NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    failure_stage VARCHAR(32),
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_transfers_source_wallet ON transfers (source_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfers_destination_wallet ON transfers (destination_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status ON transfers (status);
CREATE INDEX IF NOT EXISTS idx_transfers_correlation ON transfers (correlation_id);

CREATE TABLE IF NOT EXISTS transfer_commands (
    command_id UUID PRIMARY KEY,
    transfer_id UUID NOT NULL REFERENCES transfers (transfer_id) ON UPDATE CASCADE ON DELETE CASCADE,
    type VARCHAR(16) NOT NULL,
    wallet_id UUID NOT NULL,
    amount_minor_units BIGINT NOT NULL CHECK (amount_minor_units > 0),
    status VARCHAR(32) NOT NULL,
    last_error TEXT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    sent_at TIMESTAMPTZ,
    acknowledged_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfer_commands_transfer ON transfer_commands (transfer_id);
CREATE INDEX IF NOT EXISTS idx_transfer_commands_wallet ON transfer_commands (wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfer_commands_type ON transfer_commands (type);

CREATE TABLE IF NOT EXISTS transfer_idempotency (
    idempotency_key VARCHAR(120) PRIMARY KEY,
    transfer_id UUID NOT NULL REFERENCES transfers (transfer_id) ON UPDATE CASCADE ON DELETE CASCADE,
    request_hash CHAR(64) NOT NULL,
    response_snapshot JSONB,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfer_idempotency_transfer ON transfer_idempotency (transfer_id);
