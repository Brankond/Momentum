package com.momentum.wallet.persistence.repository;

import com.momentum.wallet.persistence.wallet.WalletEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Optional<WalletEntity> findByUserIdAndCurrencyCode(UUID userId, String currencyCode);

    List<WalletEntity> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletEntity w where w.id = :id")
    Optional<WalletEntity> findByIdForUpdate(@Param("id") UUID id);
}
