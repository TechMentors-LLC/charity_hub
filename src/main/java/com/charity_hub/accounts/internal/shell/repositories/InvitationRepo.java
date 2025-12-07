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
    public void save(Invitation invitation) {
        InvitationEntity entity = invitationMapper.toEntity(invitation);
        collection.replaceOne(
                eq("inviterId", entity.inviterId()),
                entity,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public Invitation get(String mobileNumber) {
        return Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                .map(invitationMapper::fromEntity)
                .orElse(null);
    }

    @Override
    public boolean hasInvitation(String mobileNumber) {
        return collection.find(eq("mobileNumber", mobileNumber)).first() != null;
    }

    public List<InvitationEntity> getAll() {
        return collection.find().into(new ArrayList<>());
    }
}