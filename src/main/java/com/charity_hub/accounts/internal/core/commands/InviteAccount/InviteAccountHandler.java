package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.shared.abstractions.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class InviteAccountHandler extends CommandHandler<InvitationAccount, Void> {
    private final IInvitationRepo invitationRepo;

    public InviteAccountHandler(IInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    @Override
    public CompletableFuture<Void> handle(InvitationAccount command) {
        return invitationRepo.hasInvitation(command.mobileNumber())
                .thenCompose(hasInvitation -> {
                    if (hasInvitation) {
                        return CompletableFuture.failedFuture(new AlreadyInvitedException("already invited"));
                    }

                    Invitation newInvitation = Invitation.of(
                            command.mobileNumber(),
                            command.inviterId()
                    );

                    return invitationRepo.save(newInvitation);
                });
    }
}