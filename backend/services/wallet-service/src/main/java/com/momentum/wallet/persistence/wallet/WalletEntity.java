package com.momentum.wallet.persistence.wallet;

import com.momentum.sharedkernel.domain.value.Money;
import com.momentum.wallet.persistence.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "wallets")
public class WalletEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JdbcTypeCode(Types.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "balance_minor_units", nullable = false)
    private long balanceMinorUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected WalletEntity() {
        // for JPA
    }

    public WalletEntity(UUID id, UserEntity user, Currency currency, Money initialBalance) {
        this.id = Objects.requireNonNull(id, "id");
        this.user = Objects.requireNonNull(user, "user");
        Currency resolvedCurrency = Objects.requireNonNull(currency, "currency");
        this.currencyCode = resolvedCurrency.getCurrencyCode();
        Money balance = initialBalance == null ? Money.zero(resolvedCurrency) : initialBalance;
        ensureCurrencyMatch(balance, resolvedCurrency);
        this.balanceMinorUnits = balance.toMinorUnits();
        this.status = WalletStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public Currency getCurrency() {
        return Currency.getInstance(currencyCode);
    }

    public Money getBalance() {
        return Money.ofMinor(balanceMinorUnits, getCurrency());
    }

    public WalletStatus getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void credit(Money amount) {
        ensureCurrencyMatch(amount, getCurrency());
        balanceMinorUnits += amount.toMinorUnits();
    }

    public void debit(Money amount) {
        ensureCurrencyMatch(amount, getCurrency());
        balanceMinorUnits -= amount.toMinorUnits();
    }

    public void setStatus(WalletStatus status) {
        this.status = Objects.requireNonNull(status, "status");
    }

    private void ensureCurrencyMatch(Money amount, Currency expected) {
        if (amount != null && !expected.equals(amount.currency())) {
            throw new IllegalArgumentException("Currency mismatch: expected %s but was %s".formatted(expected, amount.currency()));
        }
    }
}
