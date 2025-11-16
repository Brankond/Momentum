package com.momentum.transfer.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.transfer.config.MessagingProperties;
import com.momentum.transfer.persistence.command.TransferCommandEntity;
import com.momentum.transfer.persistence.command.TransferCommandType;
import com.momentum.transfer.persistence.transfer.TransferEntity;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class WalletCommandPublisher {

    private static final String PAYLOAD_VERSION = "1.0.0";

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;

    public WalletCommandPublisher(
            RabbitTemplate rabbitTemplate, MessagingProperties properties, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void publish(TransferCommandEntity command) {
        TransferEntity transfer = command.getTransfer();
        WalletCommandPayload payload = new WalletCommandPayload(
                command.getId(),
                transfer.getId(),
                command.getWalletId(),
                command.getAmountMinorUnits(),
                transfer.getCurrency(),
                transfer.getReference(),
                transfer.getDescription(),
                deserializeMetadata(transfer.getMetadata()));

        WalletCommandMessage message = new WalletCommandMessage(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                transfer.getCorrelationId(),
                command.getId(),
                messageType(command.getType()),
                PAYLOAD_VERSION,
                payload);

        String routingKey = command.getType() == TransferCommandType.DEBIT
                ? properties.walletDebitRoutingKey()
                : properties.walletCreditRoutingKey();
        rabbitTemplate.convertAndSend(properties.walletCommandExchange(), routingKey, message);
    }

    private String messageType(TransferCommandType type) {
        return type == TransferCommandType.DEBIT ? "wallet.debit.command" : "wallet.credit.command";
    }

    private Map<String, Object> deserializeMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse metadata JSON", e);
        }
    }

    private record WalletCommandMessage(
            UUID messageId,
            OffsetDateTime occurredAt,
            UUID correlationId,
            UUID causationId,
            String messageType,
            String payloadVersion,
            WalletCommandPayload payload) {}

    private record WalletCommandPayload(
            UUID commandId,
            UUID transferId,
            UUID walletId,
            long amountMinorUnits,
            String currency,
            String reference,
            String description,
            Map<String, Object> metadata) {}
}
