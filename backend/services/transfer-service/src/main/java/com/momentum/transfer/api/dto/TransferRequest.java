package com.momentum.transfer.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceWalletId,
        @NotNull UUID destinationWalletId,
        @Positive long amountMinorUnits,
        @NotBlank String currency,
        @NotBlank String reference,
        String description,
        Map<String, Object> metadata,
        @NotBlank String idempotencyKey) {}
