package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.accounts.internal.domain.model.account.AccountId;
import com.charity_hub.shared.domain.model.Permission;

import java.util.List;

public record PermissionRemoved(AccountId id, List<Permission> permissions) implements AccountEvent {
    public PermissionRemoved(AccountId id, List<Permission> permissions) {
        this.id = id;
        this.permissions = List.copyOf(permissions); // Creates an unmodifiable copy of the list
    }

    public static PermissionRemoved from(Account account) {
        return new PermissionRemoved(
                account.getId(),
                account.getPermissions()
        );
    }

}