package com.charity_hub.ledger.internal.domain.model;

/**
 * Type of amount tracked in the ledger.
 * - MEMBER_DUE_AMOUNT: What this member owes/should send UP to their parent
 * - NETWORK_DUE_AMOUNT: What this member expects to receive from their network
 * (children/descendants)
 */
public enum AmountType {
    MEMBER_DUE_AMOUNT,
    NETWORK_DUE_AMOUNT
}
