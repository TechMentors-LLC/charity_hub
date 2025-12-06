package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.queriers.Account;

import java.util.List;
import java.util.UUID;

public interface IAccountReadRepo {
    List<Account> getConnections(UUID id);
}