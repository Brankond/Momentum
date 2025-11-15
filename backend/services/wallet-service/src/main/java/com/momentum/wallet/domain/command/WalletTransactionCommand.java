package com.momentum.wallet.domain.command;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a debit or credit request for a wallet.
 *
 * @param entryId identifier for the resulting ledger entry
 * @param walletId target wallet
 * @param amountMinorUnits monetary value expressed in minor units; must be positive
 * @param reference idempotency key / external correlation id
 * @param description optional human-readable note
 * @param metadata optional serialized metadata payload (JSON string)
 * @param occurredAt optional timestamp override
 */
public record WalletTransactionCommand(
        UUID entryId,
        UUID walletId,
        long amountMinorUnits,
        String reference,
        String description,
        String metadata,
        OffsetDateTime occurredAt) {

    public WalletTransactionCommand {
        Objects.requireNonNull(entryId, "entryId");
        Objects.requireNonNull(walletId, "walletId");
        Objects.requireNonNull(reference, "reference");
    }
}
