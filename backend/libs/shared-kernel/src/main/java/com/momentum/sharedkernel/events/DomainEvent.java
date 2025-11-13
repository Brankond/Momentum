package com.momentum.sharedkernel.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for events that cross service boundaries.
 */
public interface DomainEvent {

    /**
     * Unique identifier that allows consumers to de-duplicate and trace events.
     *
     * @return globally unique event identifier
     */
    UUID id();

    /**
     * Timestamp recording when the event happened in the domain.
     *
     * @return event occurrence instant in UTC
     */
    Instant occurredOn();

    /**
     * Stable event type name used for routing and serialization.
     *
     * @return non-empty type string
     */
    String type();
}
