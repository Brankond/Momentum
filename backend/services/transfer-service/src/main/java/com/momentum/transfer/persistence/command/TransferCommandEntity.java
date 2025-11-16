package com.momentum.transfer.persistence.command;

import com.momentum.transfer.persistence.transfer.TransferEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "transfer_commands")
public class TransferCommandEntity {

    @Id
    @Column(name = "command_id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    private TransferEntity transfer;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private TransferCommandType type;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransferCommandStatus status = TransferCommandStatus.PENDING;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "acknowledged_at")
    private OffsetDateTime acknowledgedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected TransferCommandEntity() {
        // JPA
    }

    public TransferCommandEntity(
            UUID id,
            TransferEntity transfer,
            TransferCommandType type,
            UUID walletId,
            long amountMinorUnits) {
        if (amountMinorUnits <= 0) {
            throw new IllegalArgumentException("amountMinorUnits must be positive");
        }
        this.id = Objects.requireNonNull(id, "id");
        this.transfer = Objects.requireNonNull(transfer, "transfer");
        this.type = Objects.requireNonNull(type, "type");
        this.walletId = Objects.requireNonNull(walletId, "walletId");
        this.amountMinorUnits = amountMinorUnits;
    }

    public UUID getId() {
        return id;
    }

    public TransferEntity getTransfer() {
        return transfer;
    }

    public TransferCommandType getType() {
        return type;
    }

    public UUID getWalletId() {
        return walletId;
    }

    public long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public TransferCommandStatus getStatus() {
        return status;
    }

    public String getLastError() {
        return lastError;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public OffsetDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void markSent(OffsetDateTime sentAt) {
        this.status = TransferCommandStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markAcknowledged(OffsetDateTime acknowledgedAt) {
        this.status = TransferCommandStatus.ACKED;
        this.acknowledgedAt = acknowledgedAt;
    }

    public void markFailed(String errorMessage) {
        this.status = TransferCommandStatus.FAILED;
        this.lastError = errorMessage;
        this.retryCount++;
    }
}
