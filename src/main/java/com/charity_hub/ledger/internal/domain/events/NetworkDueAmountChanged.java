package com.charity_hub.ledger.internal.domain.events;

import com.charity_hub.ledger.internal.domain.model.Amount;
import com.charity_hub.ledger.internal.domain.model.LedgerId;

/**
 * Event raised when a member's network due amount changes.
 * Indicates the expected amount from the member's network has changed.
 */
public record NetworkDueAmountChanged(
        LedgerId ledgerId,
        Amount newNetworkDueAmount) implements LedgerEvent {

    public NetworkDueAmountChanged {
        if (ledgerId == null) {
            throw new IllegalArgumentException("LedgerId cannot be null");
        }
        if (newNetworkDueAmount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
    }
}
