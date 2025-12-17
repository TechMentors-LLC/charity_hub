package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.accounts.internal.domain.model.account.AccountId;

public record AccountBlocked(AccountId id) implements AccountEvent {
}