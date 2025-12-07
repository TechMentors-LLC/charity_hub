package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import org.springframework.stereotype.Service;

@Service
public class InviteAccountHandler extends VoidCommandHandler<InvitationAccount> {
    private final IInvitationRepo invitationRepo;

    public InviteAccountHandler(IInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    @Override
    public void handle(InvitationAccount command) {
        boolean hasInvitation = invitationRepo.hasInvitation(command.mobileNumber());

        if (hasInvitation) {
            throw new AlreadyInvitedException("already invited");
        }

        Invitation newInvitation = Invitation.of(
                command.mobileNumber(),
                command.inviterId()
        );

        invitationRepo.save(newInvitation);
    }
}