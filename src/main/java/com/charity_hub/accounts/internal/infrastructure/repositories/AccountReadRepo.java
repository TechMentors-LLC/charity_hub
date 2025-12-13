package com.charity_hub.accounts.internal.infrastructure.repositories;

import com.charity_hub.accounts.internal.application.contracts.IAccountReadRepo;
import com.charity_hub.accounts.internal.application.queries.Account;
import com.charity_hub.accounts.internal.infrastructure.db.AccountEntity;
import com.charity_hub.accounts.internal.infrastructure.db.DeviceEntity;
import com.charity_hub.accounts.internal.infrastructure.repositories.mappers.AccountReadMapper;
import com.charity_hub.accounts.shared.AccountDTO;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micrometer.observation.annotation.Observed;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

@Repository
public class AccountReadRepo implements IAccountReadRepo {

    private static final Logger logger = LoggerFactory.getLogger(AccountReadRepo.class);
    private static final String ACCOUNTS_COLLECTION = "accounts";
    private static final String CONNECTIONS_COLLECTION = "connections";

    private final MongoCollection<AccountEntity> collection;
    private final MongoCollection<Document> connectionsCollection;
    private final AccountReadMapper accountReadMapper;

    public AccountReadRepo(
            MongoDatabase mongoDatabase,
            AccountReadMapper accountReadMapper) {
        this.collection = mongoDatabase.getCollection(ACCOUNTS_COLLECTION, AccountEntity.class);
        this.connectionsCollection = mongoDatabase.getCollection(CONNECTIONS_COLLECTION);
        this.accountReadMapper = accountReadMapper;
    }

    @Override
    @Observed(name = "charity_hub.repo.account_read.get_connections", contextualName = "account-read-repo-get-connections")
    public List<Account> getConnections(UUID id) {
        // First, look up the member in the connections collection to get their children
        Document member = connectionsCollection.find(eq("_id", id.toString())).first();

        if (member == null) {
            logger.debug("No member found in connections collection for id: {}", id);
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<String> children = member.getList("children", String.class);

        if (children == null || children.isEmpty()) {
            logger.debug("Member {} has no children", id);
            return Collections.emptyList();
        }

        logger.debug("Found {} children for member {}", children.size(), id);

        // Fetch account details for all children
        return collection.find(in("accountId", children))
                .map(accountReadMapper::toQueryModel)
                .into(new ArrayList<>());
    }

    @Override
    @Cacheable(value = "accounts", key = "#id")
    @Observed(name = "charity_hub.repo.account_read.get_by_id", contextualName = "account-read-repo-get-by-id")
    public AccountDTO getById(UUID id) {
        AccountEntity entity = collection.find(eq("accountId", id.toString())).first();
        if (entity == null) {
            return null;
        }
        return toDTO(entity);
    }

    @Override
    @Observed(name = "charity_hub.repo.account_read.get_by_ids", contextualName = "account-read-repo-get-by-ids")
    public List<AccountDTO> getAccountsByIds(List<UUID> ids) {
        List<String> stringIds = ids.stream().map(UUID::toString).collect(Collectors.toList());
        return collection.find(in("accountId", stringIds))
                .map(this::toDTO)
                .into(new ArrayList<>());
    }

    private AccountDTO toDTO(AccountEntity entity) {
        return new AccountDTO(
                entity.accountId(),
                entity.mobileNumber(),
                entity.fullName() != null ? entity.fullName() : "بدون إسم",
                entity.photoUrl() != null ? entity.photoUrl() : "",
                entity.devices().stream().map(DeviceEntity::fcmToken).collect(Collectors.toList()));
    }

    @Override
    @Observed(name = "charity_hub.repo.account_read.is_admin", contextualName = "account-read-repo-is-admin")
    public boolean isAdmin(UUID id) {
        AccountEntity entity = collection.find(eq("accountId", id.toString())).first();
        if (entity == null || entity.permissions() == null) {
            return false;
        }
        return entity.permissions().contains("FULL_ACCESS");
    }
}
