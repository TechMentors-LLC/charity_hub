package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.accounts.internal.domain.model.account.AccountId;

import java.util.Objects;

public final class AccountUnBlocked implements AccountEvent {
    private final AccountId id;

    public AccountUnBlocked(AccountId id) {
        this.id = id;
    }

    public AccountId getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountUnBlocked that = (AccountUnBlocked) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}