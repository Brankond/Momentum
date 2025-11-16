package com.momentum.transfer.messaging.payload;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletTransactionResultMessage(
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
            Type type,
            Status status,
            long amountMinorUnits,
            Long runningBalanceMinorUnits,
            String reference,
            String failureReason) {}

    public enum Type {
        CREDIT,
        DEBIT
    }

    public enum Status {
        SUCCEEDED,
        FAILED
    }
}
