package com.momentum.transfer.persistence.transfer;

/**
 * Lifecycle for a transfer saga.
 */
public enum TransferStatus {
    PENDING,
    DEBIT_IN_PROGRESS,
    CREDIT_IN_PROGRESS,
    COMPENSATION_PENDING,
    FAILED_COMPENSATED,
    COMPLETED,
    FAILED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == FAILED_COMPENSATED;
    }
}
