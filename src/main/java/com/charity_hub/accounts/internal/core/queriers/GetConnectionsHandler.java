package com.charity_hub.accounts.internal.core.queriers;

import com.charity_hub.accounts.internal.core.contracts.IAccountReadRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConnectionsHandler implements QueryHandler<GetConnectionsQuery, List<Account>> {
    private final IAccountReadRepo accountReadRepo;
    private final ILogger logger;

    GetConnectionsHandler(IAccountReadRepo accountReadRepo, ILogger logger) {
        this.accountReadRepo = accountReadRepo;
        this.logger = logger;
    }

    @Override
    public List<Account> handle(GetConnectionsQuery query) {
        return accountReadRepo.getConnections(query.userId());
    }
}