package com.momentum.sharedkernel.error;

/**
 * Base exception for violations of domain invariants.
 */
public class DomainException extends RuntimeException {

    /**
     * Creates a new exception describing a domain invariant violation.
     *
     * @param message human readable failure description
     */
    public DomainException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with an underlying cause for easier debugging.
     *
     * @param message human readable failure description
     * @param cause original exception to link
     */
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
