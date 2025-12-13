package com.charity_hub.accounts.internal.infrastructure.repositories.mappers;

import com.charity_hub.accounts.internal.domain.model.invitation.Invitation;
import com.charity_hub.accounts.internal.infrastructure.db.InvitationEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InvitationMapper {

    public Invitation fromEntity(InvitationEntity entity) {
        return Invitation.of(
            entity.mobileNumber(),
            UUID.fromString(entity.inviterId())
        );
    }

    public InvitationEntity toEntity(Invitation invitation) {
        return new InvitationEntity(
            invitation.invitedMobileNumber().value(),
            invitation.inviterId().toString()
        );
    }
}