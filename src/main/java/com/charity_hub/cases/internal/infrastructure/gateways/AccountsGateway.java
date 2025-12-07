package com.charity_hub.cases.internal.infrastructure.gateways;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.accounts.shared.IAccountsAPI;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class AccountsGateway {
    private final IAccountsAPI accountsAPI;

    public AccountsGateway(IAccountsAPI accountsAPI) {
        this.accountsAPI = accountsAPI;
    }

    public List<AccountDTO> getAccountsByIdsTemp(List<UUID> idsList) {
        return accountsAPI.getAccountsByIdsTemp(idsList);
    }

    public CompletableFuture<List<AccountDTO>> getAccountsByIds(List<UUID> idsList) {
        return accountsAPI.getAccountsByIds(idsList);
    }
}