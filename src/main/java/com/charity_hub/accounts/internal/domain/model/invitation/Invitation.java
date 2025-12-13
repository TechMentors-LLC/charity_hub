package com.charity_hub.accounts.internal.domain.model.invitation;

import com.charity_hub.accounts.internal.domain.model.account.MobileNumber;

import java.util.UUID;

public record Invitation(MobileNumber invitedMobileNumber, UUID inviterId) {
    public static Invitation of(String invitedMobileNumber, UUID inviterId) {
        return new Invitation(
                MobileNumber.create(invitedMobileNumber),
                inviterId
        );
    }
}