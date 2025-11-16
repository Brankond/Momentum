package com.momentum.transfer.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.transfer.api.dto.TransferRequest;
import com.momentum.transfer.api.dto.TransferResponse;
import com.momentum.transfer.messaging.TransferEventPublisher;
import com.momentum.transfer.messaging.WalletCommandPublisher;
import com.momentum.transfer.messaging.payload.WalletTransactionResultMessage;
import com.momentum.transfer.persistence.command.TransferCommandEntity;
import com.momentum.transfer.persistence.command.TransferCommandStatus;
import com.momentum.transfer.persistence.command.TransferCommandType;
import com.momentum.transfer.persistence.idempotency.TransferIdempotencyEntity;
import com.momentum.transfer.persistence.repository.TransferCommandRepository;
import com.momentum.transfer.persistence.repository.TransferIdempotencyRepository;
import com.momentum.transfer.persistence.repository.TransferRepository;
import com.momentum.transfer.persistence.transfer.TransferEntity;
import com.momentum.transfer.persistence.transfer.TransferFailureStage;
import com.momentum.transfer.persistence.transfer.TransferStatus;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferSagaService {

    private final TransferRepository transferRepository;
    private final TransferCommandRepository transferCommandRepository;
    private final TransferIdempotencyRepository idempotencyRepository;
    private final WalletCommandPublisher walletCommandPublisher;
    private final TransferEventPublisher transferEventPublisher;
    private final ObjectMapper objectMapper;

    public TransferSagaService(
            TransferRepository transferRepository,
            TransferCommandRepository transferCommandRepository,
            TransferIdempotencyRepository idempotencyRepository,
            WalletCommandPublisher walletCommandPublisher,
            TransferEventPublisher transferEventPublisher,
            ObjectMapper objectMapper) {
        this.transferRepository = transferRepository;
        this.transferCommandRepository = transferCommandRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.walletCommandPublisher = walletCommandPublisher;
        this.transferEventPublisher = transferEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {
        Optional<TransferEntity> existing = transferRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        TransferEntity transfer = new TransferEntity(
                UUID.randomUUID(),
                request.sourceWalletId(),
                request.destinationWalletId(),
                request.amountMinorUnits(),
                request.currency(),
                request.reference(),
                request.description(),
                serializeMetadata(request.metadata()),
                UUID.randomUUID(),
                request.idempotencyKey());

        transfer.setStatus(TransferStatus.DEBIT_IN_PROGRESS);
        TransferEntity persisted = transferRepository.save(transfer);

        TransferIdempotencyEntity idempotency = new TransferIdempotencyEntity(
                request.idempotencyKey(), persisted, hashRequest(request), OffsetDateTime.now().plusDays(1));
        idempotencyRepository.save(idempotency);

        TransferCommandEntity debitCommand = transferCommandRepository.save(new TransferCommandEntity(
                UUID.randomUUID(), persisted, TransferCommandType.DEBIT, persisted.getSourceWalletId(), persisted.getAmountMinorUnits()));

        transferCommandRepository.save(new TransferCommandEntity(
                UUID.randomUUID(), persisted, TransferCommandType.CREDIT, persisted.getDestinationWalletId(), persisted.getAmountMinorUnits()));

        walletCommandPublisher.publish(debitCommand);
        return toResponse(persisted);
    }

    @Transactional
    public void handleWalletResult(WalletTransactionResultMessage message) {
        TransferEntity transfer = transferRepository
                .findById(message.payload().transferId())
                .orElseThrow(() -> new IllegalStateException("Transfer %s not found".formatted(message.payload().transferId())));

        TransferCommandEntity command = transferCommandRepository
                .findById(message.payload().commandId())
                .orElseThrow(() -> new IllegalStateException("Command %s not found".formatted(message.payload().commandId())));

        if (message.payload().status() == WalletTransactionResultMessage.Status.SUCCEEDED) {
            command.markAcknowledged(message.occurredAt());
            transferCommandRepository.save(command);
            if (command.getType() == TransferCommandType.DEBIT) {
                sendCreditCommand(transfer);
            } else if (command.getType() == TransferCommandType.CREDIT) {
                markCompleted(transfer, message.occurredAt());
            } else if (command.getType() == TransferCommandType.REVERSAL) {
                finalizeCompensation(transfer);
            }
        } else {
            command.markFailed(message.payload().failureReason());
            transferCommandRepository.save(command);
            markFailed(transfer, command.getType(), message.payload().failureReason());
        }
    }

    private void sendCreditCommand(TransferEntity transfer) {
        transfer.setStatus(TransferStatus.CREDIT_IN_PROGRESS);
        transferRepository.save(transfer);
        transferCommandRepository.findByTransfer_IdAndType(transfer.getId(), TransferCommandType.CREDIT).stream()
                .filter(command -> command.getStatus() == TransferCommandStatus.PENDING)
                .findFirst()
                .ifPresent(walletCommandPublisher::publish);
    }

    private void markCompleted(TransferEntity transfer, OffsetDateTime completedAt) {
        transfer.markCompleted(completedAt);
        transferRepository.save(transfer);
        transferEventPublisher.publishCompleted(transfer);
    }

    private void markFailed(TransferEntity transfer, TransferCommandType stage, String reason) {
        if (stage == TransferCommandType.CREDIT) {
            transfer.markCompensationPending(reason);
            transferRepository.save(transfer);
            issueCompensationCommand(transfer, reason);
            transferEventPublisher.publishCompensation(transfer, reason);
            return;
        }

        TransferFailureStage failureStage =
                stage == TransferCommandType.DEBIT ? TransferFailureStage.DEBIT : TransferFailureStage.REVERSAL;

        transfer.markFailure(failureStage, reason);
        transferRepository.save(transfer);
        transferEventPublisher.publishFailed(transfer, reason);
    }

    private void issueCompensationCommand(TransferEntity transfer, String reason) {
        TransferCommandEntity reversal = transferCommandRepository.save(new TransferCommandEntity(
                UUID.randomUUID(),
                transfer,
                TransferCommandType.REVERSAL,
                transfer.getSourceWalletId(),
                transfer.getAmountMinorUnits()));
        walletCommandPublisher.publish(reversal);
    }

    private void finalizeCompensation(TransferEntity transfer) {
        transfer.markFailureCompensated(transfer.getFailureReason());
        transferRepository.save(transfer);
        transferEventPublisher.publishFailed(transfer, transfer.getFailureReason());
    }

    private TransferResponse toResponse(TransferEntity transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceWalletId(),
                transfer.getDestinationWalletId(),
                transfer.getAmountMinorUnits(),
                transfer.getCurrency(),
                transfer.getReference(),
                transfer.getDescription(),
                deserializeMetadata(transfer.getMetadata()),
                transfer.getStatus(),
                transfer.getFailureStage(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getUpdatedAt(),
                transfer.getCompletedAt());
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize metadata", e);
        }
    }

    private Map<String, Object> deserializeMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize metadata", e);
        }
    }

    private String hashRequest(TransferRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = objectMapper.writeValueAsBytes(request);
            byte[] hash = digest.digest(bytes);
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            throw new IllegalStateException("Unable to hash transfer request", e);
        }
    }
}
