package com.momentum.wallet.domain.command;

import com.momentum.sharedkernel.domain.value.Money;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

/**
 * Command payload for provisioning a new wallet.
 *
 * @param walletId unique identifier for the wallet row
 * @param userId owner identifier; will be created if missing
 * @param externalUserId human-friendly identifier (email/phone)
 * @param currency wallet currency, stored as ISO code
 * @param initialBalance optional seed balance; defaults to zero when {@code null}
 */
public record CreateWalletCommand(
        UUID walletId,
        UUID userId,
        String externalUserId,
        Currency currency,
        Money initialBalance) {

    public CreateWalletCommand {
        Objects.requireNonNull(walletId, "walletId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(externalUserId, "externalUserId");
        Objects.requireNonNull(currency, "currency");
    }
}
