package com.charity_hub.ledger.internal.domain.model;

import com.charity_hub.shared.domain.model.ValueObject;

import java.util.UUID;

/**
 * Value object representing Ledger aggregate identity.
 * Maps 1:1 with MemberId (each member has exactly one ledger).
 */
public record LedgerId(UUID value) implements ValueObject {

    public LedgerId {
        if (value == null) {
            throw new IllegalArgumentException("LedgerId value cannot be null");
        }
    }

    public static LedgerId generate() {
        return new LedgerId(UUID.randomUUID());
    }

    public static LedgerId from(UUID id) {
        return new LedgerId(id);
    }

    public static LedgerId from(String id) {
        return new LedgerId(UUID.fromString(id));
    }

    public static LedgerId fromMemberId(MemberId memberId) {
        return new LedgerId(memberId.value());
    }
}
