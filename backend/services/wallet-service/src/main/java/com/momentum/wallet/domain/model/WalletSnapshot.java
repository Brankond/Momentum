package com.momentum.wallet.domain.model;

import com.momentum.wallet.persistence.wallet.WalletStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable representation of wallet state returned by domain services.
 */
public record WalletSnapshot(
        UUID walletId,
        UUID userId,
        String currency,
        long balanceMinorUnits,
        WalletStatus status,
        OffsetDateTime updatedAt) {
}
