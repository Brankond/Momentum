package com.momentum.transfer.persistence.command;

/**
 * State of a command dispatched to the wallet service.
 */
public enum TransferCommandStatus {
    PENDING,
    SENT,
    ACKED,
    FAILED
}
