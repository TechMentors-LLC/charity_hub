package com.charity_hub.accounts.internal.application.contracts;

import com.charity_hub.accounts.internal.domain.model.account.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountRepo {
    Optional<Account> getById(UUID id);

    List<Account> getConnections(UUID id);

    Optional<Account> getByMobileNumber(String mobileNumber);

    void save(Account account);

    boolean isAdmin(String mobileNumber);

    void revoke(UUID uuid);

    boolean isRevoked(UUID id, long tokenIssueDate);
}