package com.charity_hub.accounts.internal.application.contracts;

import com.charity_hub.accounts.internal.application.queries.Account;
import com.charity_hub.accounts.shared.AccountDTO;

import java.util.List;
import java.util.UUID;

public interface IAccountReadRepo {
    List<Account> getConnections(UUID id);

    AccountDTO getById(UUID id);

    List<AccountDTO> getAccountsByIds(List<UUID> ids);

    boolean isAdmin(UUID id);
}