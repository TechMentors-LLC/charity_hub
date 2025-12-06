package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.shared.abstractions.CommandHandler;
import org.springframework.stereotype.Service;

@Service
public class InviteAccountHandler extends CommandHandler<InvitationAccount, Void> {
    private final IInvitationRepo invitationRepo;

    public InviteAccountHandler(IInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    @Override
    public Void handle(InvitationAccount command) {
        boolean hasInvitation = invitationRepo.hasInvitation(command.mobileNumber());

        if (hasInvitation) {
            throw new AlreadyInvitedException("already invited");
        }

        Invitation newInvitation = Invitation.of(
                command.mobileNumber(),
                command.inviterId()
        );

        invitationRepo.save(newInvitation);

        return null;
    }
}