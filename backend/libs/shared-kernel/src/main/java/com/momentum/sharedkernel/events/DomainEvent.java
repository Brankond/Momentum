package com.momentum.sharedkernel.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for events that cross service boundaries.
 */
public interface DomainEvent {

    UUID id();

    Instant occurredOn();

    String type();
}
