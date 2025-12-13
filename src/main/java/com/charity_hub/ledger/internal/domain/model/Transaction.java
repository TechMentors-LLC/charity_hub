package com.charity_hub.ledger.internal.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a ledger transaction.
 * Records debits and credits to member accounts with full audit trail.
 */
public class Transaction {
    private final MemberId memberId;
    private final TransactionType type;
    private final Service service;
    private final Amount amount;
    private final Instant timestamp;

    public Transaction(
            MemberId memberId,
            TransactionType type,
            Service service,
            Amount amount,
            Instant timestamp) {
        if (memberId == null) {
            throw new IllegalArgumentException("MemberId cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("TransactionType cannot be null");
        }
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        this.memberId = memberId;
        this.type = type;
        this.service = service;
        this.amount = amount;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Transaction createDebit(MemberId memberId, Service service, Amount amount) {
        return new Transaction(memberId, TransactionType.DEBIT, service, amount, Instant.now());
    }

    public static Transaction createCredit(MemberId memberId, Service service, Amount amount) {
        return new Transaction(memberId, TransactionType.CREDIT, service, amount, Instant.now());
    }

    public MemberId getMemberId() {
        return memberId;
    }

    public TransactionType getType() {
        return type;
    }

    public Service getService() {
        return service;
    }

    public Amount getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Transaction that = (Transaction) o;
        return Objects.equals(memberId, that.memberId) &&
                type == that.type &&
                Objects.equals(service, that.service) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, type, service, amount, timestamp);
    }
}
