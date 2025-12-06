package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.account.Account;

import java.util.Optional;
import java.util.UUID;

public interface IAccountRepo {
    Optional<Account> getById(UUID id);

    Optional<Account> getByMobileNumber(String mobileNumber);

    void save(Account account);

    boolean isAdmin(String mobileNumber);

    void revoke(UUID uuid);

    boolean isRevoked(UUID id, long tokenIssueDate);
}