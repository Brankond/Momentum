package com.momentum.transfer.persistence.transfer;

/**
 * Identifies which step of the saga failed.
 */
public enum TransferFailureStage {
    DEBIT,
    CREDIT,
    REVERSAL,
    UNKNOWN
}
