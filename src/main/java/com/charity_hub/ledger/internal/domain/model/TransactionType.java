package com.charity_hub.ledger.internal.domain.model;

/**
 * Type of ledger transaction.
 * - DEBIT: Reduces the balance (payment made, obligation fulfilled)
 * - CREDIT: Increases the balance (obligation created, amount received)
 */
public enum TransactionType {
    DEBIT,
    CREDIT
}
