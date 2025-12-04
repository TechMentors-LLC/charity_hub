package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.accounts.internal.shell.repositories.mappers.InvitationMapper;
import com.charity_hub.accounts.internal.shell.db.InvitationEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;

import io.micrometer.observation.annotation.Observed;

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
    @Observed(name = "db.invitation.save", contextualName = "mongo-save-invitation")
    public void save(Invitation invitation) {
            InvitationEntity entity = invitationMapper.toEntity(invitation);
            collection.replaceOne(
                    eq("inviterId", entity.inviterId()), // assuming getId() returns the document ID
                    entity,
                    new ReplaceOptions().upsert(true)
            );
    }

    @Override
    @Observed(name = "db.invitation.get", contextualName = "mongo-get-invitation")
    public Optional<Invitation> get(String mobileNumber) {
        return
                Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                        .map(invitationMapper::fromEntity);
    }

    @Override
    @Observed(name = "db.invitation.hasInvitation", contextualName = "mongo-has-invitation")
    public boolean hasInvitation(String mobileNumber) {
        return collection.find(eq("mobileNumber", mobileNumber)).first() != null       ;
       
    }

    @Override
    @Observed(name = "db.invitation.getAll", contextualName = "mongo-get-all-invitations")
    public List<Invitation> getAll() {
        return collection.find().map(invitationMapper::fromEntity).into(new ArrayList<>());
    }
}