package com.momentum.wallet.persistence.repository;

import com.momentum.wallet.persistence.ledger.LedgerEntryEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    List<LedgerEntryEntity> findByWalletIdOrderByOccurredAtAsc(UUID walletId);

    Optional<LedgerEntryEntity> findByWalletIdAndReference(UUID walletId, String reference);
}
