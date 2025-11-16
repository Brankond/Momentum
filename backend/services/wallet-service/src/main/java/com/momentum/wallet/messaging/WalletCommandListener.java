package com.momentum.wallet.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.momentum.sharedkernel.error.DomainException;
import com.momentum.wallet.domain.command.WalletTransactionCommand;
import com.momentum.wallet.domain.service.WalletDomainService;
import com.momentum.wallet.domain.model.LedgerEntrySnapshot;
import com.momentum.wallet.messaging.payload.WalletCommandMessage;
import java.time.OffsetDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class WalletCommandListener {

    private static final Logger log = LoggerFactory.getLogger(WalletCommandListener.class);

    private final WalletDomainService walletDomainService;
    private final WalletEventPublisher walletEventPublisher;
    private final ObjectMapper objectMapper;

    public WalletCommandListener(
            WalletDomainService walletDomainService, WalletEventPublisher walletEventPublisher, ObjectMapper objectMapper) {
        this.walletDomainService = walletDomainService;
        this.walletEventPublisher = walletEventPublisher;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${wallet.messaging.command-queue}")
    public void handle(WalletCommandMessage message) {
        String type = resolveType(message);
        try {
            WalletTransactionCommand command = toCommand(message);
            LedgerEntrySnapshot snapshot = switch (type) {
                case "wallet.debit.command" -> walletDomainService.debit(command);
                case "wallet.credit.command" -> walletDomainService.credit(command);
                default -> throw new IllegalArgumentException("Unsupported command type: " + type);
            };
            walletEventPublisher.publishSuccess(message, mapResultType(type), snapshot);
        } catch (DomainException | IllegalArgumentException ex) {
            log.warn("Wallet command failed: {}", ex.getMessage());
            walletEventPublisher.publishFailure(message, mapResultType(type), ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected error handling wallet command", ex);
            walletEventPublisher.publishFailure(message, mapResultType(type), "Internal error");
        }
    }

    private WalletTransactionCommand toCommand(WalletCommandMessage message) {
        try {
            String metadata = serializeMetadata(message.payload().metadata());
            return new WalletTransactionCommand(
                    message.payload().commandId(),
                    message.payload().walletId(),
                    message.payload().amountMinorUnits(),
                    message.payload().reference(),
                    message.payload().description(),
                    metadata,
                    message.occurredAt() != null ? message.occurredAt() : OffsetDateTime.now());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid metadata payload", e);
        }
    }

    private String serializeMetadata(Map<String, Object> metadata) throws JsonProcessingException {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(metadata);
    }

    private String resolveType(WalletCommandMessage message) {
        return message.messageType();
    }

    private String mapResultType(String messageType) {
        return messageType.contains("debit") ? "DEBIT" : "CREDIT";
    }
}
