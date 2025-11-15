package com.momentum.wallet.domain.model;

import com.momentum.wallet.persistence.ledger.LedgerEntryType;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable view of a persisted ledger entry.
 */
public record LedgerEntrySnapshot(
        UUID entryId,
        UUID walletId,
        LedgerEntryType type,
        long amountMinorUnits,
        long runningBalanceMinorUnits,
        String reference,
        String description,
        String metadata,
        OffsetDateTime occurredAt) {
}
