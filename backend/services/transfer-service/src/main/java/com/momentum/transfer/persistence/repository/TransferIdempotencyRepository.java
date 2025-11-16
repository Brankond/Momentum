package com.momentum.transfer.persistence.repository;

import com.momentum.transfer.persistence.idempotency.TransferIdempotencyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferIdempotencyRepository extends JpaRepository<TransferIdempotencyEntity, String> {

    Optional<TransferIdempotencyEntity> findByTransfer_Id(UUID transferId);
}
