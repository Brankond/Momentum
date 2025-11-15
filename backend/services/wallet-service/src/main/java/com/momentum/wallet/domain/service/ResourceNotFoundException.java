package com.momentum.wallet.domain.service;

import com.momentum.sharedkernel.error.DomainException;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
