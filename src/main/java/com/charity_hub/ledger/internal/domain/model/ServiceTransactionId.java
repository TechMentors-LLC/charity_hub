package com.charity_hub.ledger.internal.domain.model;

import java.util.UUID;

/**
 * Value object wrapping the external service's transaction ID.
 * Links ledger transactions to their originating service transactions (e.g.,
 * contribution ID).
 */
public record ServiceTransactionId(UUID value) {

    public ServiceTransactionId {
        if (value == null) {
            throw new IllegalArgumentException("ServiceTransactionId value cannot be null");
        }
    }

    public static ServiceTransactionId from(UUID id) {
        return new ServiceTransactionId(id);
    }

    public static ServiceTransactionId from(String id) {
        return new ServiceTransactionId(UUID.fromString(id));
    }
}
