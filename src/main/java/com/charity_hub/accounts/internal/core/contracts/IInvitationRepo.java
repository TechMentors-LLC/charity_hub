package com.charity_hub.accounts.internal.core.contracts;

import com.charity_hub.accounts.internal.core.model.invitation.Invitation;

public interface IInvitationRepo {
    void save(Invitation invitation);

    Invitation get(String mobileNumber);

    boolean hasInvitation(String mobileNumber);
}