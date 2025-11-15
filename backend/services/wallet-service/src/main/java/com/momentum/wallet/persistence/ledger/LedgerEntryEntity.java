package com.momentum.wallet.persistence.ledger;

import com.momentum.wallet.persistence.wallet.WalletEntity;
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

@Entity
@Table(name = "ledger_entries")
public class LedgerEntryEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 24)
    private LedgerEntryType type;

    @Column(name = "amount_minor_units", nullable = false)
    private long amountMinorUnits;

    @Column(name = "running_balance_minor_units", nullable = false)
    private long runningBalanceMinorUnits;

    @Column(name = "reference", nullable = false, length = 120)
    private String reference;

    @Column(name = "description")
    private String description;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    protected LedgerEntryEntity() {
        // JPA
    }

    public LedgerEntryEntity(
            UUID id,
            WalletEntity wallet,
            LedgerEntryType type,
            long amountMinorUnits,
            long runningBalanceMinorUnits,
            String reference,
            String description,
            String metadata,
            OffsetDateTime occurredAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.wallet = Objects.requireNonNull(wallet, "wallet");
        this.type = Objects.requireNonNull(type, "type");
        this.reference = Objects.requireNonNull(reference, "reference");
        this.amountMinorUnits = amountMinorUnits;
        this.runningBalanceMinorUnits = runningBalanceMinorUnits;
        this.description = description;
        this.metadata = metadata;
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }

    public UUID getId() {
        return id;
    }

    public WalletEntity getWallet() {
        return wallet;
    }

    public LedgerEntryType getType() {
        return type;
    }

    public long getAmountMinorUnits() {
        return amountMinorUnits;
    }

    public long getRunningBalanceMinorUnits() {
        return runningBalanceMinorUnits;
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

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
