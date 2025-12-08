package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IAccountReadRepo;
import com.charity_hub.accounts.internal.core.queriers.Account;
import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.charity_hub.accounts.internal.shell.repositories.mappers.AccountReadMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class AccountReadRepo implements IAccountReadRepo {

    private static final String ACCOUNTS_COLLECTION = "accounts";

    private final MongoCollection<AccountEntity> collection;
    private final AccountReadMapper accountReadMapper;

    public AccountReadRepo(
            MongoDatabase mongoDatabase,
            AccountReadMapper accountReadMapper
    ) {
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
        this.accountReadMapper = accountReadMapper;
    }

    @Override
    @Timed(value = "charity_hub.repo.account_read.get_connections", description = "Time taken to fetch account connections")
    public List<Account> getConnections(UUID id) {
        return collection.find(eq("connections.userId", id.toString()))
                .map(accountReadMapper::toQueryModel)
                .into(new ArrayList<>());
    }
}
