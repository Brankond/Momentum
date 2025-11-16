package com.momentum.transfer.api.dto;

import com.momentum.transfer.persistence.transfer.TransferFailureStage;
import com.momentum.transfer.persistence.transfer.TransferStatus;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record TransferResponse(
        UUID transferId,
        UUID sourceWalletId,
        UUID destinationWalletId,
        long amountMinorUnits,
        String currency,
        String reference,
        String description,
        Map<String, Object> metadata,
        TransferStatus status,
        TransferFailureStage failureStage,
        String failureReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime completedAt) {}
