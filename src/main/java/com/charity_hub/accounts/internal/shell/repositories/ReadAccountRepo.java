package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@Repository
public class ReadAccountRepo {
    private static final Logger logger = LoggerFactory.getLogger(ReadAccountRepo.class);

    public static final String ACCOUNTS_COLLECTION = "accounts";
    private final MongoCollection<AccountEntity> collection;

    public ReadAccountRepo(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
    }

    @Timed(value = "charity_hub.repo.read_account.get_by_id", description = "Time taken to fetch account by ID")
    public AccountEntity getById(UUID id) {
        logger.debug("Looking up account by id: {}", id);
        AccountEntity entity = collection.find(eq("accountId", id.toString())).first();
        if (entity == null) {
            logger.debug("Account not found with id: {}", id);
        }
        return entity;
    }

    @Timed(value = "charity_hub.repo.read_account.get_by_ids", description = "Time taken to fetch accounts by IDs")
    public List<AccountEntity> getAccountsByIds(List<UUID> ids) {
        logger.debug("Looking up accounts by {} ids", ids.size());
        List<AccountEntity> accounts = collection.find(in("accountId", ids)).into(new ArrayList<>());
        logger.debug("Found {} accounts for {} ids", accounts.size(), ids.size());
        return accounts;
    }
}