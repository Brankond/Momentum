package com.momentum.wallet.messaging.payload;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record WalletCommandMessage(
        UUID messageId,
        OffsetDateTime occurredAt,
        UUID correlationId,
        UUID causationId,
        String messageType,
        String payloadVersion,
        Payload payload) {

    public record Payload(
            UUID commandId,
            UUID transferId,
            UUID walletId,
            long amountMinorUnits,
            String currency,
            String reference,
            String description,
            Map<String, Object> metadata) {}
}
