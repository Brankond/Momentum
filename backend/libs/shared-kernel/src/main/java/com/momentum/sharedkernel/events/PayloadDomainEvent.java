package com.momentum.sharedkernel.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple immutable event implementation with a typed payload.
 *
 * @param <T> payload type
 * @param id unique identifier
 * @param occurredOn event occurrence instant
 * @param type event type name
 * @param payload domain payload
 */
public record PayloadDomainEvent<T>(UUID id, Instant occurredOn, String type, T payload) implements DomainEvent {

    public PayloadDomainEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(occurredOn, "occurredOn");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(payload, "payload");
    }

    /**
     * Creates a new event using random identifiers and {@link Instant#now()}.
     *
     * @param type event type name
     * @param payload domain payload
     * @return new {@link PayloadDomainEvent}
     */
    public static <T> PayloadDomainEvent<T> of(String type, T payload) {
        return new PayloadDomainEvent<>(UUID.randomUUID(), Instant.now(), type, payload);
    }
}
