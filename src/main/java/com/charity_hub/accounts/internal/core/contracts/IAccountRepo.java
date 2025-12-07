package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.account.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAccountRepo {
    Optional<Account> getByIdTemp(UUID id);

    CompletableFuture<Account> getById(UUID id);

    CompletableFuture<List<Account>> getConnections(UUID id);

    Optional<Account> getByMobileNumberTemp(String mobileNumber);
    CompletableFuture<Account> getByMobileNumber(String mobileNumber);

    void saveTemp(Account account);
    CompletableFuture<Void> save(Account account);

    boolean isAdminTemp(String mobileNumber);

    CompletableFuture<Boolean> isAdmin(String mobileNumber);

    CompletableFuture<Void> revoke(UUID uuid);

    CompletableFuture<Boolean> isRevoked(UUID id, long tokenIssueDate);
}