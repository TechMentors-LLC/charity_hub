package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@Repository
public class ReadAccountRepo {

    public static final String ACCOUNTS_COLLECTION = "accounts";
    private final MongoCollection<AccountEntity> collection;

    public ReadAccountRepo(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
    }

    public CompletableFuture<AccountEntity> getById(UUID id) {
        return CompletableFuture.supplyAsync(() ->
                collection.find(eq("accountId", id.toString())).first()
        );
    }

    public CompletableFuture<List<AccountEntity>> getAccountsByIds(List<UUID> ids) {
        return CompletableFuture.supplyAsync(() -> collection.find(in("accountId", ids)).into(new ArrayList<>())
        );
    }
}