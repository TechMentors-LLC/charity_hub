package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.accounts.internal.domain.model.account.AccountId;
import com.charity_hub.accounts.internal.domain.model.account.MobileNumber;

public record AccountCreated(AccountId id, MobileNumber mobileNumber) implements AccountEvent {

    public static AccountCreated from(Account account) {
        return new AccountCreated(
                account.getId(),
                account.getMobileNumber()
        );
    }

}