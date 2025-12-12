package com.charity_hub.accounts.internal.domain.exceptions;

import com.charity_hub.shared.exceptions.BusinessRuleException;

public class AlreadyInvitedException extends BusinessRuleException {
    public AlreadyInvitedException(String message) {
        super(message);
    }
}