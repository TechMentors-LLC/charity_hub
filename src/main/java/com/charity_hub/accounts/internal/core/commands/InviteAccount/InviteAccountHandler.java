package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import com.charity_hub.shared.abstractions.CommandHandler;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class InviteAccountHandler extends CommandHandler<InvitationAccount, Void> {
    private final IInvitationRepo invitationRepo;

    public InviteAccountHandler(IInvitationRepo invitationRepo) {
        this.invitationRepo = invitationRepo;
    }

    @Override
    public CompletableFuture<Void> handle(InvitationAccount command) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (invitationRepo.hasInvitation(command.mobileNumber()).get()) {
                    throw new AlreadyInvitedException("already invited");
                }

                Invitation newInvitation = Invitation.of(
                        command.mobileNumber(),  // invitedMobileNumber
                        command.inviterId()      // inviterId
                );

                invitationRepo.save(newInvitation);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}