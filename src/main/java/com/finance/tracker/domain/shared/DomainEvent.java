package com.finance.tracker.domain.shared;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Getter
@AllArgsConstructor
public class DomainEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Instant occurredAt;
    private final Long aggregateId;

    protected DomainEvent(Long aggregateId) {
        this.aggregateId= aggregateId;
        this.occurredAt = Instant.now();
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "occurredAt=" + occurredAt +
                ", aggregateId=" + aggregateId +
                '}';
    }
}
