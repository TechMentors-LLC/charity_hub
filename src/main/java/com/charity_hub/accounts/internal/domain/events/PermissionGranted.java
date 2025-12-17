package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.accounts.internal.domain.model.account.AccountId;
import com.charity_hub.shared.domain.model.Permission;

import java.util.List;

public record PermissionGranted(AccountId id, List<Permission> permissions) implements AccountEvent {
    public PermissionGranted(AccountId id, List<Permission> permissions) {
        this.id = id;
        this.permissions = List.copyOf(permissions); // Creates an unmodifiable copy of the list
    }

    public static PermissionGranted from(Account account) {
        return new PermissionGranted(
                account.getId(),
                account.getPermissions()
        );
    }

}