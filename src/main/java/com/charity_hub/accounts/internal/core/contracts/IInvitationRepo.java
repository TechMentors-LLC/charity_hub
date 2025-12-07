package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.invitation.Invitation;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IInvitationRepo {
    void saveTemp(Invitation invitation);
    CompletableFuture<Void> save(Invitation invitation);

    CompletableFuture<Invitation> get(String mobileNumber);

    boolean hasInvitationTemp(String mobileNumber);
    CompletableFuture<Boolean> hasInvitation(String mobileNumber);
}