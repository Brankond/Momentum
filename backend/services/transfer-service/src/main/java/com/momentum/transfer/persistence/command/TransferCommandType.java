package com.momentum.transfer.persistence.command;

/**
 * Command types emitted to the wallet service.
 */
public enum TransferCommandType {
    DEBIT,
    CREDIT,
    REVERSAL
}
