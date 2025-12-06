package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.events.AccountEvent;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.charity_hub.accounts.internal.shell.db.RevokedAccountEntity;
import com.charity_hub.accounts.internal.shell.repositories.mappers.AccountEventsMapper;
import com.charity_hub.accounts.internal.shell.repositories.mappers.DomainAccountMapper;
import com.charity_hub.shared.domain.IEventBus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import io.micrometer.observation.annotation.Observed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.mongodb.client.model.Filters.*;

@Repository
public class AccountRepo implements IAccountRepo {
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
        this.admins = Collections.unmodifiableList(admins);
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
        this.revokedCollection = mongoDatabase.getCollection(REVOKED_ACCOUNT_COLLECTION, RevokedAccountEntity.class);
        this.eventBus = eventBus;
        this.domainAccountMapper = domainAccountMapper;
    }

    @Override
    @Observed(name = "db.account.get", contextualName = "mongo-get-account")
    public Optional<Account> getById(UUID id) {
        return Optional.ofNullable(collection.find(eq("accountId", id.toString())).first())
                .map(domainAccountMapper::toDomain);
    }


    @Override
    @Observed(name = "db.account.getByMobileNumber", contextualName = "mongo-get-account-by-mobile-number")
    public Optional<Account> getByMobileNumber(String mobileNumber) {
        return Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                .map(domainAccountMapper::toDomain);
    }

    @Override
    @Observed(name = "db.account.save", contextualName = "mongo-save-account")
    public void save(Account account) {
        AccountEntity entity = domainAccountMapper.toDB(account);
        collection.replaceOne(
                eq("accountId", entity.accountId()),
                entity,
                new ReplaceOptions().upsert(true)
        );
        account.occurredEvents().stream()
                .map(event -> AccountEventsMapper.map((AccountEvent) event))
                .forEach(eventBus::push);
    }

    @Override
    public boolean isAdmin(String mobileNumber) {
        return admins.contains(mobileNumber);
    }

    @Override
    @Observed(name = "db.account.revoke", contextualName = "mongo-revoke-account")
    public void revoke(UUID uuid) {
        RevokedAccountEntity revokedAccount = new RevokedAccountEntity(uuid.toString(), new Date().getTime());
        if (revokedCollection.find(eq("accountId", uuid.toString())).first() != null) {
            revokedCollection.replaceOne(eq("accountId", uuid.toString()), revokedAccount);
        } else {
            revokedCollection.insertOne(revokedAccount);
        }
    }

    @Override
    @Observed(name = "db.account.isRevoked", contextualName = "mongo-account-is-revoked")
    public boolean isRevoked(UUID id, long tokenIssueDate) {
        return
                revokedCollection.find(and(
                        eq("accountId", id.toString()),
                        gt("revokedTime", tokenIssueDate)
                )).first() != null;
    }
}