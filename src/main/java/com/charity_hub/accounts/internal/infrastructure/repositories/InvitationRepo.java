package com.charity_hub.accounts.internal.infrastructure.repositories;

import com.charity_hub.accounts.internal.application.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.domain.model.invitation.Invitation;
import com.charity_hub.accounts.internal.infrastructure.repositories.mappers.InvitationMapper;
import com.charity_hub.accounts.internal.infrastructure.db.InvitationEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class InvitationRepo implements IInvitationRepo {
    private static final Logger logger = LoggerFactory.getLogger(InvitationRepo.class);
    private static final String COLLECTION = "invitations";

    private final MongoCollection<InvitationEntity> collection;
    private final InvitationMapper invitationMapper;

    public InvitationRepo(MongoDatabase mongoDatabase, InvitationMapper invitationMapper) {
        this.collection = mongoDatabase.getCollection(COLLECTION, InvitationEntity.class);
        this.invitationMapper = invitationMapper;
    }

    @Override
    @Observed(name = "charity_hub.repo.invitation.save", contextualName = "invitation-repo-save")
    public void save(Invitation invitation) {
        logger.info("Saving invitation for mobile: {}", invitation.invitedMobileNumber().value());
        InvitationEntity entity = invitationMapper.toEntity(invitation);
        collection.replaceOne(
                eq("inviterId", entity.inviterId()),
                entity,
                new ReplaceOptions().upsert(true)
        );
        logger.debug("Invitation saved successfully for mobile: {}", invitation.invitedMobileNumber().value());
    }

    @Override
    @Observed(name = "charity_hub.repo.invitation.get", contextualName = "invitation-repo-get")
    public Invitation get(String mobileNumber) {
        logger.debug("Looking up invitation for mobile: {}", mobileNumber);
        Invitation invitation = Optional.ofNullable(collection.find(eq("mobileNumber", mobileNumber)).first())
                .map(invitationMapper::fromEntity)
                .orElse(null);
        if (invitation == null) {
            logger.debug("No invitation found for mobile: {}", mobileNumber);
        }
        return invitation;
    }

    @Override
    @Observed(name = "charity_hub.repo.invitation.has_invitation", contextualName = "invitation-repo-has-invitation")
    public boolean hasInvitation(String mobileNumber) {
        logger.debug("Checking if invitation exists for mobile: {}", mobileNumber);
        boolean exists = collection.find(eq("mobileNumber", mobileNumber)).first() != null;
        logger.debug("Invitation exists for mobile {}: {}", mobileNumber, exists);
        return exists;
    }

    public List<InvitationEntity> getAll() {
        logger.debug("Retrieving all invitations");
        List<InvitationEntity> invitations = collection.find().into(new ArrayList<>());
        logger.debug("Retrieved {} invitations", invitations.size());
        return invitations;
    }
}