package com.charity_hub.accounts.internal.infrastructure.repositories;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.domain.events.AccountEvent;
import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.accounts.internal.infrastructure.repositories.mappers.AccountEventsMapper;
import com.charity_hub.accounts.internal.infrastructure.repositories.mappers.DomainAccountMapper;
import com.charity_hub.accounts.internal.infrastructure.db.AccountEntity;
import com.charity_hub.accounts.internal.infrastructure.db.RevokedAccountEntity;
import com.charity_hub.shared.domain.IEventBus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

@Repository
public class AccountRepo implements IAccountRepo {
    private static final Logger logger = LoggerFactory.getLogger(AccountRepo.class);
    private static final String ACCOUNTS_COLLECTION = "accounts";
    private static final String REVOKED_ACCOUNT_COLLECTION = "revoked_accounts";

    private final MongoCollection<AccountEntity> collection;
    private final MongoCollection<RevokedAccountEntity> revokedCollection;
    private final List<String> admins;
    private final IEventBus eventBus;
    private final DomainAccountMapper domainAccountMapper;

    public AccountRepo(
            @Value("${accounts.admins}") List<String> admins,
            MongoDatabase mongoDatabase,
            IEventBus eventBus,
            DomainAccountMapper domainAccountMapper
    ) {
        this.admins = admins;
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
        this.revokedCollection = mongoDatabase.getCollection(REVOKED_ACCOUNT_COLLECTION, RevokedAccountEntity.class);
        this.eventBus = eventBus;
        this.domainAccountMapper = domainAccountMapper;
    }

    @Override
    @Observed(name = "charity_hub.repo.account.get_by_id", contextualName = "account-repo-get-by-id")
    public Optional<Account> getById(UUID id) {
        logger.debug("Fetching account by ID: {}", id);
        return Optional.ofNullable(collection.find(eq("accountId", id.toString())).first())
                .map(domainAccountMapper::toDomain);
    }

    @Override
    @Observed(name = "charity_hub.repo.account.get_connections", contextualName = "account-repo-get-connections")
    public List<Account> getConnections(UUID id) {
        logger.debug("Fetching connections for account: {}", id);
        return collection.find(eq("connections.userId", id.toString()))
                .map(domainAccountMapper::toDomain)
                .into(new ArrayList<>());
    }

    @Override
    @Observed(name = "charity_hub.repo.account.get_by_mobile", contextualName = "account-repo-get-by-mobile")
    public Optional<Account> getByMobileNumber(String mobileNumber) {
        logger.debug("Fetching account by mobile number: {}", mobileNumber);
        return Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                .map(domainAccountMapper::toDomain);
    }

    @Override
    @Observed(name = "charity_hub.repo.account.save", contextualName = "account-repo-save")
    public void save(Account account) {
        logger.debug("Saving account: {}", account.getId().value());
        AccountEntity entity = domainAccountMapper.toDB(account);
        collection.replaceOne(
                eq("accountId", entity.accountId()),
                entity,
                new ReplaceOptions().upsert(true)
        );
        logger.info("Account saved successfully: {}", account.getId().value());
        account.occurredEvents().stream()
                .map(event -> AccountEventsMapper.map((AccountEvent) event))
                .forEach(eventBus::push);
    }

    @Override
    public boolean isAdmin(String mobileNumber) {
        boolean isAdmin = admins.contains(mobileNumber);
        logger.debug("Admin check for {}: {}", mobileNumber, isAdmin);
        return isAdmin;
    }

    @Override
    public void revoke(UUID uuid) {
        logger.info("Revoking account tokens: {}", uuid);
        RevokedAccountEntity revokedAccount = new RevokedAccountEntity(uuid.toString(), new Date().getTime());
        if (revokedCollection.find(eq("accountId", uuid.toString())).first() != null) {
            revokedCollection.replaceOne(eq("accountId", uuid.toString()), revokedAccount);
        } else {
            revokedCollection.insertOne(revokedAccount);
        }
        logger.info("Account tokens revoked: {}", uuid);
    }

    @Override
    public boolean isRevoked(UUID id, long tokenIssueDate) {
        boolean isRevoked = revokedCollection.find(and(
                eq("accountId", id.toString()),
                gt("revokedTime", tokenIssueDate)
        )).first() != null;
        if (isRevoked) {
            logger.debug("Token revoked for account: {}", id);
        }
        return isRevoked;
    }
}