package com.charity_hub.accounts.internal.domain.model.account;

import com.charity_hub.shared.domain.model.ValueObject;

import java.util.UUID;

public record AccountId(UUID value) implements ValueObject {

    // Static factory method (replacing companion object)
    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }
}