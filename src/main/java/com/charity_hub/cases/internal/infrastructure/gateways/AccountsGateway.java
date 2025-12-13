package com.charity_hub.cases.internal.infrastructure.gateways;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.accounts.shared.IAccountsAPI;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("casesAccountsGateway")
public class AccountsGateway {
    private final IAccountsAPI accountsAPI;

    public AccountsGateway(IAccountsAPI accountsAPI) {
        this.accountsAPI = accountsAPI;
    }

    public List<AccountDTO> getAccountsByIds(List<UUID> idsList) {
        return accountsAPI.getAccountsByIds(idsList);
    }

    public boolean isAdmin(UUID userId) {
        return accountsAPI.isAdmin(userId);
    }
}