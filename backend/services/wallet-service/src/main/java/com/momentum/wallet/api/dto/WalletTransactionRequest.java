package com.momentum.wallet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WalletTransactionRequest(
        @NotNull @Positive Long amountMinorUnits,
        @NotBlank String reference,
        String description,
        String metadata) {
}
