package com.charity_hub.ledger.internal.domain.events;

import com.charity_hub.ledger.internal.domain.model.Transaction;

/**
 * Event raised when a transaction is created in the ledger.
 * Used for audit trail and potential integration with other systems.
 */
public record TransactionCreated(Transaction transaction) implements LedgerEvent {

    public TransactionCreated {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
    }
}
