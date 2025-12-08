package com.charity_hub.accounts.internal.core.queriers;

import com.charity_hub.accounts.internal.core.contracts.IAccountReadRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetConnectionsHandler implements QueryHandler<GetConnectionsQuery, List<Account>> {
    private final IAccountReadRepo accountReadRepo;

    GetConnectionsHandler(IAccountReadRepo accountReadRepo) {
        this.accountReadRepo = accountReadRepo;
    }

    @Override
    @Observed(name = "handler.get_connections", contextualName = "get-connections-handler")
    public List<Account> handle(GetConnectionsQuery query) {
        return accountReadRepo.getConnections(query.userId());
    }
}