package com.charity_hub.ledger.internal.domain.model;

import com.charity_hub.ledger.internal.domain.exceptions.InvalidAmountException;

import java.util.Objects;

/**
 * Value object representing a monetary amount in the ledger.
 * Supports arithmetic operations and validation.
 */
public record Amount(int value, AmountType type) {

    private static final int MIN_AMOUNT = -9999999;
    private static final int MAX_AMOUNT = 9999999;

    public Amount {
        if (value < 0) {
            throw new InvalidAmountException(
                    String.format("%d is invalid, should be within %d - %d", value, MIN_AMOUNT, MAX_AMOUNT));
        }
    }

    public static Amount forNetwork(int value) {
        return new Amount(value, AmountType.NETWORK_DUE_AMOUNT);
    }

    public static Amount forMember(int value) {
        return new Amount(value, AmountType.MEMBER_DUE_AMOUNT);
    }

    public static Amount zero(AmountType type) {
        return new Amount(0, type);
    }

    public Amount plus(Amount increment) {
        if (!Objects.equals(this.type, increment.type)) {
            throw new InvalidAmountException("Cannot add amounts of different types");
        }
        return new Amount(this.value + increment.value, this.type);
    }

    public Amount minus(Amount decrement) {
        if (!Objects.equals(this.type, decrement.type)) {
            throw new InvalidAmountException("Cannot subtract amounts of different types");
        }
        return new Amount(this.value - decrement.value, this.type);
    }
}
