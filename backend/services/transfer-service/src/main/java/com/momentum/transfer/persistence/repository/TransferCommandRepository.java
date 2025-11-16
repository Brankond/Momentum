package com.momentum.transfer.persistence.repository;

import com.momentum.transfer.persistence.command.TransferCommandEntity;
import com.momentum.transfer.persistence.command.TransferCommandStatus;
import com.momentum.transfer.persistence.command.TransferCommandType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferCommandRepository extends JpaRepository<TransferCommandEntity, UUID> {

    List<TransferCommandEntity> findByTransfer_Id(UUID transferId);

    List<TransferCommandEntity> findByTransfer_IdAndType(UUID transferId, TransferCommandType type);

    List<TransferCommandEntity> findByStatus(TransferCommandStatus status);
}
