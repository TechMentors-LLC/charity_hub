package com.charity_hub.accounts.internal.infrastructure.repositories.mappers;

import com.charity_hub.accounts.internal.application.queries.Account;
import com.charity_hub.accounts.internal.infrastructure.db.AccountEntity;
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
