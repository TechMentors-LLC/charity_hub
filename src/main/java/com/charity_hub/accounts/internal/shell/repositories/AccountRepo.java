package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.events.AccountEvent;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.core.model.account.AccountId;
import com.charity_hub.accounts.internal.core.model.account.ConnectionsInfo;
import com.charity_hub.accounts.internal.shell.repositories.mappers.AccountEventsMapper;
import com.charity_hub.accounts.internal.shell.repositories.mappers.DomainAccountMapper;
import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.charity_hub.accounts.internal.shell.db.RevokedAccountEntity;
import com.charity_hub.shared.domain.IEventBus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
        this.admins = admins;
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
        this.revokedCollection = mongoDatabase.getCollection(REVOKED_ACCOUNT_COLLECTION, RevokedAccountEntity.class);
        this.eventBus = eventBus;
        this.domainAccountMapper = domainAccountMapper;
    }

    @Override
    public CompletableFuture<Account> getById(UUID id) {
        return CompletableFuture.supplyAsync(() ->
                Optional.ofNullable(collection.find(eq("accountId", id.toString())).first())
                        .map(domainAccountMapper::toDomain)
                        .orElse(null)
        );
    }

    @Override
    public CompletableFuture<ConnectionsInfo> getConnections(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            AccountEntity accountEntity = collection.find(eq("accountId", id.toString())).first();
            if (accountEntity == null || accountEntity.connections() == null) {
                return new ConnectionsInfo(null, List.of());
            }

            // Parent
            ConnectionsInfo.MinimalAccount parent = null;
            String parentId = accountEntity.connections().parent();
            if (parentId != null && !parentId.isEmpty()) {
                AccountEntity parentEntity = collection.find(eq("accountId", parentId)).first();
                if (parentEntity != null) {
                    String parentFullName = parentEntity.fullName() != null && !parentEntity.fullName().isEmpty()
                        ? parentEntity.fullName() : "No Name"; // Default fallback
                    parent = new ConnectionsInfo.MinimalAccount(
                        new AccountId(UUID.fromString(parentEntity.accountId())),
                        parentEntity.mobileNumber(),
                        parentFullName
                    );
                }
            }

            // Children (batch query for efficiency)
            List<String> childrenIds = accountEntity.connections().children() != null
                    ? accountEntity.connections().children()
                    : List.of();
            List<ConnectionsInfo.MinimalAccount> children = new ArrayList<>();
            if (!childrenIds.isEmpty()) {
                // Batch query for all children
                for (AccountEntity childEntity : collection.find(in("accountId", childrenIds))) {
                    if (childEntity == null) continue;
                    String childFullName = childEntity.fullName() != null && !childEntity.fullName().isEmpty()
                        ? childEntity.fullName() : "No Name"; // Default fallback
                    children.add(new ConnectionsInfo.MinimalAccount(
                        new AccountId(UUID.fromString(childEntity.accountId())),
                        childEntity.mobileNumber(),
                        childFullName
                    ));
                }
            }

            return new ConnectionsInfo(parent, children);
        });
    }

    @Override
    public CompletableFuture<Account> getByMobileNumber(String mobileNumber) {
        return CompletableFuture.supplyAsync(() ->
                Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                        .map(domainAccountMapper::toDomain)
                        .orElse(null)
        );
    }

    @Override
    public CompletableFuture<Void> save(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            AccountEntity entity = domainAccountMapper.toDB(account);
            collection.replaceOne(
                    eq("accountId", entity.accountId()),
                    entity,
                    new ReplaceOptions().upsert(true)
            );
            account.occurredEvents().stream()
                    .map(event -> AccountEventsMapper.map((AccountEvent) event))
                    .forEach(eventBus::push);
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> isAdmin(String mobileNumber) {
        return CompletableFuture.completedFuture(admins.contains(mobileNumber));
    }

    @Override
    public CompletableFuture<Void> revoke(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            RevokedAccountEntity revokedAccount = new RevokedAccountEntity(uuid.toString(), new Date().getTime());
            if (revokedCollection.find(eq("accountId", uuid.toString())).first() != null) {
                revokedCollection.replaceOne(eq("accountId", uuid.toString()), revokedAccount);
            } else {
                revokedCollection.insertOne(revokedAccount);
            }
            return null;
        });
    }

    @Override
    public CompletableFuture<Boolean> isRevoked(UUID id, long tokenIssueDate) {
        return CompletableFuture.supplyAsync(() ->
                revokedCollection.find(and(
                        eq("accountId", id.toString()),
                        gt("revokedTime", tokenIssueDate)
                )).first() != null
        );
    }
}