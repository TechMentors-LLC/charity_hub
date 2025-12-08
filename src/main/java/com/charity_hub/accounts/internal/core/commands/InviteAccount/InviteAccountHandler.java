package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class InviteAccountHandler extends VoidCommandHandler<InvitationAccount> {
    private final IInvitationRepo invitationRepo;

    public InviteAccountHandler(IInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    @Override
    @Timed(value = "charity_hub.handler.invite_account", description = "Time taken by InviteAccountHandler")
    @Observed(name = "handler.invite_account", contextualName = "invite-account-handler")
    public void handle(InvitationAccount command) {
        logger.info("Processing invitation - MobileNumber: {}, InviterId: {}", 
                command.mobileNumber(), command.inviterId());
        
        boolean hasInvitation = invitationRepo.hasInvitation(command.mobileNumber());

        if (hasInvitation) {
            logger.warn("Duplicate invitation attempt - MobileNumber: {} already invited", command.mobileNumber());
            throw new AlreadyInvitedException("already invited");
        }

        Invitation newInvitation = Invitation.of(
                command.mobileNumber(),
                command.inviterId()
        );

        invitationRepo.save(newInvitation);
        logger.info("Invitation created successfully - MobileNumber: {}, InviterId: {}", 
                command.mobileNumber(), command.inviterId());
    }
}