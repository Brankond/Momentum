package com.momentum.wallet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record CreateWalletRequest(
        @NotNull UUID userId,
        @NotBlank String externalUserId,
        @NotBlank String currency,
        @PositiveOrZero Long initialBalanceMinorUnits) {
}
