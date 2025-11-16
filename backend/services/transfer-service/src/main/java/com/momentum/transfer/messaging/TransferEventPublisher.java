package com.momentum.transfer.messaging;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.transfer.config.MessagingProperties;
import com.momentum.transfer.persistence.transfer.TransferEntity;
import com.momentum.transfer.persistence.transfer.TransferFailureStage;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class TransferEventPublisher {

    private static final String PAYLOAD_VERSION = "1.0.0";

    private final RabbitTemplate rabbitTemplate;
    private final MessagingProperties properties;
    private final ObjectMapper objectMapper;

    public TransferEventPublisher(
            RabbitTemplate rabbitTemplate, MessagingProperties properties, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public void publishCompleted(TransferEntity transfer) {
        CompletedPayload payload = new CompletedPayload(
                transfer.getId(),
                transfer.getSourceWalletId(),
                transfer.getDestinationWalletId(),
                transfer.getAmountMinorUnits(),
                transfer.getCurrency(),
                transfer.getReference(),
                transfer.getDescription(),
                deserializeMetadata(transfer.getMetadata()),
                transfer.getCompletedAt() != null ? transfer.getCompletedAt() : OffsetDateTime.now());

        Envelope<CompletedPayload> message = new Envelope<>(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                transfer.getCorrelationId(),
                transfer.getId(),
                properties.transferCompletedRoutingKey(),
                PAYLOAD_VERSION,
                payload);

        rabbitTemplate.convertAndSend(properties.transferEventExchange(), properties.transferCompletedRoutingKey(), message);
    }

    public void publishFailed(TransferEntity transfer, String failureReason) {
        FailedPayload payload = new FailedPayload(
                transfer.getId(),
                transfer.getSourceWalletId(),
                transfer.getDestinationWalletId(),
                transfer.getAmountMinorUnits(),
                transfer.getCurrency(),
                transfer.getReference(),
                transfer.getDescription(),
                deserializeMetadata(transfer.getMetadata()),
                transfer.getFailureStage() != null ? transfer.getFailureStage() : TransferFailureStage.UNKNOWN,
                failureReason,
                OffsetDateTime.now());

        Envelope<FailedPayload> message = new Envelope<>(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                transfer.getCorrelationId(),
                transfer.getId(),
                properties.transferFailedRoutingKey(),
                PAYLOAD_VERSION,
                payload);

        rabbitTemplate.convertAndSend(properties.transferEventExchange(), properties.transferFailedRoutingKey(), message);
    }

    public void publishCompensation(TransferEntity transfer, String reason) {
        CompensationPayload payload = new CompensationPayload(
                transfer.getId(),
                transfer.getSourceWalletId(),
                transfer.getDestinationWalletId(),
                transfer.getAmountMinorUnits(),
                transfer.getCurrency(),
                transfer.getReference(),
                deserializeMetadata(transfer.getMetadata()),
                reason,
                OffsetDateTime.now());

        Envelope<CompensationPayload> message = new Envelope<>(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                transfer.getCorrelationId(),
                transfer.getId(),
                properties.transferCompensationRoutingKey(),
                PAYLOAD_VERSION,
                payload);

        rabbitTemplate.convertAndSend(
                properties.transferEventExchange(), properties.transferCompensationRoutingKey(), message);
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

    private record Envelope<T>(
            UUID messageId,
            OffsetDateTime occurredAt,
            UUID correlationId,
            UUID causationId,
            String messageType,
            String payloadVersion,
            T payload) {}

    private record CompletedPayload(
            UUID transferId,
            UUID debitWalletId,
            UUID creditWalletId,
            long amountMinorUnits,
            String currency,
            String reference,
            String description,
            Map<String, Object> metadata,
            OffsetDateTime completedAt) {}

    private record FailedPayload(
            UUID transferId,
            UUID debitWalletId,
            UUID creditWalletId,
            long amountMinorUnits,
            String currency,
            String reference,
            String description,
            Map<String, Object> metadata,
            TransferFailureStage failureStage,
            String failureReason,
            OffsetDateTime failedAt) {}

    private record CompensationPayload(
            UUID transferId,
            UUID debitWalletId,
            UUID creditWalletId,
            long amountMinorUnits,
            String currency,
            String reference,
            Map<String, Object> metadata,
            String reason,
            OffsetDateTime requestedAt) {}
}
