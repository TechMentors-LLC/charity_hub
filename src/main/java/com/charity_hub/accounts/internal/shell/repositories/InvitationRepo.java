package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.accounts.internal.shell.repositories.mappers.InvitationMapper;
import com.charity_hub.accounts.internal.shell.db.InvitationEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class InvitationRepo implements IInvitationRepo {
    private static final String COLLECTION = "invitations";

    private final MongoCollection<InvitationEntity> collection;
    private final InvitationMapper invitationMapper;

    public InvitationRepo(MongoDatabase mongoDatabase, InvitationMapper invitationMapper) {
        this.collection = mongoDatabase.getCollection(COLLECTION, InvitationEntity.class);
        this.invitationMapper = invitationMapper;
    }

    @Override
    public CompletableFuture<Void> save(Invitation invitation) {
        return CompletableFuture.supplyAsync(() -> {
            InvitationEntity entity = invitationMapper.toEntity(invitation);
            collection.replaceOne(
                    eq("inviterId", entity.inviterId()), // assuming getId() returns the document ID
                    entity,
                    new ReplaceOptions().upsert(true)
            );
            return null;
        });
    }
    @Override
    public void saveTemp(Invitation invitation) {
            InvitationEntity entity = invitationMapper.toEntity(invitation);
            collection.replaceOne(
                    eq("inviterId", entity.inviterId()), // assuming getId() returns the document ID
                    entity,
                    new ReplaceOptions().upsert(true)
            );
    }

    @Override
    public CompletableFuture<Invitation> get(String mobileNumber) {
        return CompletableFuture.supplyAsync(() ->
                Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                        .map(invitationMapper::fromEntity)
                        .orElse(null)
        );
    }

    @Override
    public CompletableFuture<Boolean> hasInvitation(String mobileNumber) {
        return CompletableFuture.supplyAsync(() ->
                collection.find(eq("mobileNumber", mobileNumber)).first() != null
        );
    }
    @Override
    public boolean hasInvitationTemp(String mobileNumber) {
              return  collection.find(eq("mobileNumber", mobileNumber)).first() != null;
    }

    public CompletableFuture<List<InvitationEntity>> getAll() {
        return CompletableFuture.supplyAsync(() ->
                collection.find().into(new ArrayList<>())
        );
    }
}