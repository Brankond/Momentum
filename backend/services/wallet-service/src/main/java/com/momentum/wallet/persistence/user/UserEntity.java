package com.momentum.wallet.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true, length = 120)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private UserStatus status = UserStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserEntity() {
        // for JPA
    }

    public UserEntity(UUID id, String externalId, UserStatus status) {
        this.id = Objects.requireNonNull(id, "id");
        this.externalId = Objects.requireNonNull(externalId, "externalId");
        this.status = Objects.requireNonNullElse(status, UserStatus.ACTIVE);
    }

    public UUID getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setExternalId(String externalId) {
        this.externalId = Objects.requireNonNull(externalId, "externalId");
    }

    public void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }
}
