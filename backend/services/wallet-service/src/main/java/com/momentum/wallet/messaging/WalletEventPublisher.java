package com.momentum.wallet.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.wallet.config.WalletMessagingProperties;
import com.momentum.wallet.domain.model.LedgerEntrySnapshot;
import com.momentum.wallet.messaging.payload.WalletCommandMessage;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletEventPublisher {

    private static final String PAYLOAD_VERSION = "1.0.0";

    private final RabbitTemplate rabbitTemplate;
    private final WalletMessagingProperties properties;

    public WalletEventPublisher(
            RabbitTemplate rabbitTemplate, WalletMessagingProperties properties, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    public void publishSuccess(WalletCommandMessage message, String commandType, LedgerEntrySnapshot snapshot) {
        Payload payload = new Payload(
                message.payload().commandId(),
                message.payload().transferId(),
                message.payload().walletId(),
                snapshot.entryId(),
                commandType,
                "SUCCEEDED",
                snapshot.amountMinorUnits(),
                snapshot.runningBalanceMinorUnits(),
                message.payload().reference(),
                null);

        Envelope envelope = new Envelope(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                message.correlationId(),
                message.messageId(),
                properties.transactionResultRoutingKey(),
                PAYLOAD_VERSION,
                payload);

        rabbitTemplate.convertAndSend(properties.eventExchange(), properties.transactionResultRoutingKey(), envelope);
    }

    public void publishFailure(WalletCommandMessage message, String commandType, String failureReason) {
        Payload payload = new Payload(
                message.payload().commandId(),
                message.payload().transferId(),
                message.payload().walletId(),
                null,
                commandType,
                "FAILED",
                message.payload().amountMinorUnits(),
                null,
                message.payload().reference(),
                failureReason);

        Envelope envelope = new Envelope(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                message.correlationId(),
                message.messageId(),
                properties.transactionResultRoutingKey(),
                PAYLOAD_VERSION,
                payload);

        rabbitTemplate.convertAndSend(properties.eventExchange(), properties.transactionResultRoutingKey(), envelope);
    }

    private record Envelope(
            UUID messageId,
            OffsetDateTime occurredAt,
            UUID correlationId,
            UUID causationId,
            String messageType,
            String payloadVersion,
            Payload payload) {}

    private record Payload(
            UUID commandId,
            UUID transferId,
            UUID walletId,
            UUID entryId,
            String type,
            String status,
            long amountMinorUnits,
            Long runningBalanceMinorUnits,
            String reference,
            String failureReason) {}
}
