package com.charity_hub.accounts.internal.shell.repositories.mappers;

import com.charity_hub.accounts.internal.core.queriers.Account;
import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountReadMapper {
    public Account toQueryModel(AccountEntity entity) {
        return new Account(
                entity.accountId(),
                entity.fullName() != null ? entity.fullName() : "Unknown",
                entity.photoUrl(),
                entity.permissions()
        );
    }
}
