package com.charity_hub.accounts.internal.core.model.account;

import java.util.List;

public record ConnectionsInfo(MinimalAccount parent, List<MinimalAccount> children) {
    
    public record MinimalAccount(AccountId id, String mobileNumber, String fullName) {}
}
