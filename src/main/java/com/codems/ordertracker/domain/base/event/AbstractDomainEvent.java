package com.codems.ordertracker.domain.base.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;

    protected AbstractDomainEvent() {
        this(UUID.randomUUID(), Instant.now());
    }

    protected AbstractDomainEvent(UUID eventId, Instant occurredAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
    }

    @Override
    public final UUID eventId() {
        return eventId;
    }

    @Override
    public final Instant occurredAt() {
        return occurredAt;
    }
}
