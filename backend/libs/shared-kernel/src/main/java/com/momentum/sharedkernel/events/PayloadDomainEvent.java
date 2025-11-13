package com.momentum.sharedkernel.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple immutable event implementation with a typed payload.
 */
public record PayloadDomainEvent<T>(UUID id, Instant occurredOn, String type, T payload) implements DomainEvent {

    public PayloadDomainEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(occurredOn, "occurredOn");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
    }

    public static <T> PayloadDomainEvent<T> of(String type, T payload) {
        return new PayloadDomainEvent<>(UUID.randomUUID(), Instant.now(), type, payload);
    }
}
