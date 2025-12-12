package com.charity_hub.ledger.internal.domain.events;

import com.charity_hub.shared.domain.model.DomainEvent;

/**
 * Sealed interface for all ledger domain events.
 */
public sealed interface LedgerEvent extends DomainEvent
        permits DueAmountChanged, NetworkDueAmountChanged, TransactionCreated {
}
