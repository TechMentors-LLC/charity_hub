package com.charity_hub.accounts.internal.core.queriers;

import java.util.concurrent.CompletableFuture;

import com.charity_hub.accounts.internal.core.model.account.ConnectionsInfo;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Service;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.QueryHandler;

@Service
public class GetConnectionsHandler implements QueryHandler<GetConnectionsQuery, ConnectionsInfo> {
    private final IAccountRepo accountRepo;
    private final ILogger logger;

    public GetConnectionsHandler(IAccountRepo accountRepo, ILogger logger) {
        this.accountRepo = accountRepo;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<ConnectionsInfo> handle(GetConnectionsQuery query) {
        return accountRepo.getConnections(query.userId())
                .exceptionally(throwable -> {
                    logger.error("Failed to get connections for user: {}", query.userId(), throwable);
                    throw new RuntimeException("Failed to fetch connections", throwable);
                });
    }
}