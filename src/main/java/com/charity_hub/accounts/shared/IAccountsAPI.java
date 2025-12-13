package com.charity_hub.accounts.shared;

import java.util.List;
import java.util.UUID;

public interface IAccountsAPI {
    InvitationResponse getInvitationByMobileNumber(String mobileNumber);

    AccountDTO getById(UUID id);

    List<AccountDTO> getAccountsByIds(List<UUID> idsList);

    boolean isAdmin(UUID userId);
}