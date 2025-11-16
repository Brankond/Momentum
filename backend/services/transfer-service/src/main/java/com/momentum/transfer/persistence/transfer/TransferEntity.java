package com.momentum.transfer.persistence.transfer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    @Column(name = "transfer_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "source_wallet_id", nullable = false)
    private UUID sourceWalletId;

    @Column(name = "destination_wallet_id", nullable = false)
    private UUID destinationWalletId;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "reference", nullable = false, length = 120)
    private String reference;

    @Column(name = "description")
    private String description;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TransferStatus status = TransferStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_stage", length = 32)
    private TransferFailureStage failureStage;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    protected TransferEntity() {
        // JPA
    }

    public TransferEntity(
            UUID id,
            UUID sourceWalletId,
            UUID destinationWalletId,
            long amountMinorUnits,
            String currency,
            String reference,
            String description,
            String metadata,
            UUID correlationId,
            String idempotencyKey) {
        this.id = Objects.requireNonNull(id, "id");
        this.sourceWalletId = Objects.requireNonNull(sourceWalletId, "sourceWalletId");
        this.destinationWalletId = Objects.requireNonNull(destinationWalletId, "destinationWalletId");
        if (amountMinorUnits <= 0) {
            throw new IllegalArgumentException("amountMinorUnits must be positive");
        }
        this.amountMinorUnits = amountMinorUnits;
        this.currency = Objects.requireNonNull(currency, "currency");
        this.reference = Objects.requireNonNull(reference, "reference");
        this.description = description;
        this.metadata = metadata;
        this.correlationId = Objects.requireNonNull(correlationId, "correlationId");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        this.status = TransferStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSourceWalletId() {
        return sourceWalletId;
    }

    public UUID getDestinationWalletId() {
        return destinationWalletId;
    }

    public long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public String getCurrency() {
        return currency;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public String getMetadata() {
        return metadata;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public TransferFailureStage getFailureStage() {
        return failureStage;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setStatus(TransferStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    public void markFailure(TransferFailureStage stage, String reason) {
        this.failureStage = stage;
        this.failureReason = reason;
        this.status = TransferStatus.FAILED;
    }

    public void markFailureCompensated(String reason) {
        this.failureStage = TransferFailureStage.CREDIT;
        this.failureReason = reason;
        this.status = TransferStatus.FAILED_COMPENSATED;
        this.completedAt = OffsetDateTime.now();
    }

    public void markCompleted(OffsetDateTime completedAt) {
        this.status = TransferStatus.COMPLETED;
        this.failureStage = null;
        this.failureReason = null;
        this.completedAt = completedAt;
    }

    public void markCompensationPending(String reason) {
        this.status = TransferStatus.COMPENSATION_PENDING;
        this.failureStage = TransferFailureStage.CREDIT;
        this.failureReason = reason;
    }
}
