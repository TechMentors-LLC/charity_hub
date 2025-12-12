package com.charity_hub.accounts.internal.application.contracts;

import com.charity_hub.accounts.internal.domain.model.invitation.Invitation;

public interface IInvitationRepo {
    void save(Invitation invitation);

    Invitation get(String mobileNumber);

    boolean hasInvitation(String mobileNumber);
}