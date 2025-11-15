package com.momentum.wallet.domain.service;

import com.momentum.sharedkernel.domain.value.Money;
import com.momentum.sharedkernel.error.DomainException;
import com.momentum.wallet.domain.command.CreateWalletCommand;
import com.momentum.wallet.domain.command.WalletTransactionCommand;
import com.momentum.wallet.domain.model.LedgerEntrySnapshot;
import com.momentum.wallet.domain.model.WalletSnapshot;
import com.momentum.wallet.persistence.ledger.LedgerEntryEntity;
import com.momentum.wallet.persistence.ledger.LedgerEntryType;
import com.momentum.wallet.persistence.repository.LedgerEntryRepository;
import com.momentum.wallet.persistence.repository.UserRepository;
import com.momentum.wallet.persistence.repository.WalletRepository;
import com.momentum.wallet.persistence.user.UserEntity;
import com.momentum.wallet.persistence.user.UserStatus;
import com.momentum.wallet.persistence.wallet.WalletEntity;
import com.momentum.wallet.persistence.wallet.WalletStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application-layer service that coordinates wallet persistence, ledger writes, and idempotency.
 */
@Service
public class WalletDomainService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletDomainService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            LedgerEntryRepository ledgerEntryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public WalletSnapshot createWallet(CreateWalletCommand command) {
        UserEntity owner = userRepository
                .findById(command.userId())
                .orElseGet(() -> userRepository.save(new UserEntity(command.userId(), command.externalUserId(), UserStatus.ACTIVE)));

        walletRepository
                .findByUserIdAndCurrencyCode(owner.getId(), command.currency().getCurrencyCode())
                .ifPresent(existing -> {
                    throw new DomainException("Wallet already exists for user %s in currency %s"
                            .formatted(owner.getId(), command.currency().getCurrencyCode()));
                });

        Money initialBalance = command.initialBalance() == null
                ? Money.zero(command.currency())
                : command.initialBalance();
        WalletEntity wallet = new WalletEntity(
                command.walletId(),
                owner,
                command.currency(),
                initialBalance);

        WalletEntity persisted = walletRepository.save(wallet);
        return toWalletSnapshot(persisted);
    }

    @Transactional(readOnly = true)
    public WalletSnapshot getWallet(UUID walletId) {
        WalletEntity wallet = walletRepository
                .findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet %s not found".formatted(walletId)));
        return toWalletSnapshot(wallet);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntrySnapshot> getLedger(UUID walletId) {
        ensureWalletExists(walletId);
        return ledgerEntryRepository.findByWalletIdOrderByOccurredAtAsc(walletId).stream()
                .map(this::toLedgerEntrySnapshot)
                .collect(Collectors.toList());
    }

    @Transactional
    public LedgerEntrySnapshot credit(WalletTransactionCommand command) {
        return applyTransaction(command, LedgerEntryType.CREDIT);
    }

    @Transactional
    public LedgerEntrySnapshot debit(WalletTransactionCommand command) {
        return applyTransaction(command, LedgerEntryType.DEBIT);
    }

    private LedgerEntrySnapshot applyTransaction(WalletTransactionCommand command, LedgerEntryType type) {
        WalletEntity wallet = walletRepository
                .findByIdForUpdate(command.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet %s not found".formatted(command.walletId())));

        ensureWalletActive(wallet);
        enforcePositiveAmount(command.amountMinorUnits());

        return ledgerEntryRepository
                .findByWalletIdAndReference(wallet.getId(), command.reference())
                .map(this::toLedgerEntrySnapshot)
                .orElseGet(() -> processTransaction(command, type, wallet));
    }

    private LedgerEntrySnapshot processTransaction(
            WalletTransactionCommand command, LedgerEntryType type, WalletEntity wallet) {
        Money amount = Money.ofMinor(command.amountMinorUnits(), wallet.getCurrency());
        if (type == LedgerEntryType.DEBIT) {
            ensureSufficientBalance(wallet, amount);
            wallet.debit(amount);
        } else if (type == LedgerEntryType.CREDIT) {
            wallet.credit(amount);
        } else {
            throw new DomainException("Unsupported ledger entry type: %s".formatted(type));
        }

        LedgerEntryEntity entry = new LedgerEntryEntity(
                command.entryId(),
                wallet,
                type,
                amount.toMinorUnits(),
                wallet.getBalance().toMinorUnits(),
                command.reference(),
                command.description(),
                command.metadata(),
                Objects.requireNonNullElseGet(command.occurredAt(), OffsetDateTime::now));

        LedgerEntryEntity savedEntry = ledgerEntryRepository.save(entry);
        walletRepository.save(wallet);
        return toLedgerEntrySnapshot(savedEntry);
    }

    private void ensureWalletExists(UUID walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new ResourceNotFoundException("Wallet %s not found".formatted(walletId));
        }
    }

    private void ensureWalletActive(WalletEntity wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new DomainException("Wallet %s is not active".formatted(wallet.getId()));
        }
    }

    private void enforcePositiveAmount(long amountMinorUnits) {
        if (amountMinorUnits <= 0) {
            throw new DomainException("Amount must be greater than zero");
        }
    }

    private void ensureSufficientBalance(WalletEntity wallet, Money amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new DomainException("Insufficient funds for wallet %s".formatted(wallet.getId()));
        }
    }

    private WalletSnapshot toWalletSnapshot(WalletEntity wallet) {
        return new WalletSnapshot(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getCurrency().getCurrencyCode(),
                wallet.getBalance().toMinorUnits(),
                wallet.getStatus(),
                wallet.getUpdatedAt());
    }

    private LedgerEntrySnapshot toLedgerEntrySnapshot(LedgerEntryEntity entity) {
        return new LedgerEntrySnapshot(
                entity.getId(),
                entity.getWallet().getId(),
                entity.getType(),
                entity.getAmountMinorUnits(),
                entity.getRunningBalanceMinorUnits(),
                entity.getReference(),
                entity.getDescription(),
                entity.getMetadata(),
                entity.getOccurredAt());
    }
}
