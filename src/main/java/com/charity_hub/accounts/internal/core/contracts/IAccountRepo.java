package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.account.Account;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAccountRepo {
    CompletableFuture<Account> getById(UUID id);

    CompletableFuture<List<Account>> getConnections(UUID id);

    CompletableFuture<Account> getByMobileNumber(String mobileNumber);

    CompletableFuture<Void> save(Account account);

    CompletableFuture<Boolean> isAdmin(String mobileNumber);

    CompletableFuture<Void> revoke(UUID uuid);

    CompletableFuture<Boolean> isRevoked(UUID id, long tokenIssueDate);
}