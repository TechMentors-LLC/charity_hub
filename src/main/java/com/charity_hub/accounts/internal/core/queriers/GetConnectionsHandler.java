package com.charity_hub.accounts.internal.core.queriers;

import java.util.List;

import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Service;

import com.charity_hub.accounts.internal.core.contracts.IAccountReadRepo;
import com.charity_hub.shared.abstractions.QueryHandler;

@Service
public class GetConnectionsHandler implements QueryHandler<GetConnectionsQuery, List<Account>> {
    private final IAccountReadRepo accountRepo;
    private final ILogger logger;

    GetConnectionsHandler(IAccountReadRepo accountRepo, ILogger logger) {
        this.accountRepo = accountRepo;
        this.logger = logger;
    }

    @Override
    public List<Account> handle(GetConnectionsQuery query) {
        return accountRepo.getConnections(query.userId());
    }
}