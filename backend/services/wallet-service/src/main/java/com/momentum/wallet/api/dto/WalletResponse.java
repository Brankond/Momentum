package com.momentum.wallet.api.dto;

import com.momentum.wallet.persistence.wallet.WalletStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(
        UUID walletId,
        UUID userId,
        String currency,
        long balanceMinorUnits,
        WalletStatus status,
        OffsetDateTime updatedAt) {
}
