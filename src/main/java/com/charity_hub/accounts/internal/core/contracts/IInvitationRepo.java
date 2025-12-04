package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.invitation.Invitation;

import java.util.List;
import java.util.Optional;


public interface IInvitationRepo {
    void save(Invitation invitation);

    Optional<Invitation> get(String mobileNumber);

    boolean hasInvitation(String mobileNumber);

    List<Invitation> getAll();
}