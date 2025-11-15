package com.momentum.wallet.api.dto;

import com.momentum.wallet.persistence.ledger.LedgerEntryType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LedgerEntryResponse(
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
