package com.charity_hub.ledger.internal.domain.events;

import com.charity_hub.ledger.internal.domain.model.Amount;
import com.charity_hub.ledger.internal.domain.model.LedgerId;

/**
 * Event raised when a member's due amount changes.
 * Indicates the member's obligation to their parent has changed.
 */
public record DueAmountChanged(
        LedgerId ledgerId,
        Amount newDueAmount) implements LedgerEvent {

    public DueAmountChanged {
        if (ledgerId == null) {
            throw new IllegalArgumentException("LedgerId cannot be null");
        }
        if (newDueAmount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
    }
}
