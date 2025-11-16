package com.momentum.transfer.persistence.repository;

import com.momentum.transfer.persistence.transfer.TransferEntity;
import com.momentum.transfer.persistence.transfer.TransferStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    Optional<TransferEntity> findByIdempotencyKey(String idempotencyKey);

    Optional<TransferEntity> findByCorrelationId(UUID correlationId);

    List<TransferEntity> findByStatus(TransferStatus status);
}
