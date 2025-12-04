package com.charity_hub.accounts.shared;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountsAPI {
    Optional<InvitationResponse> getInvitationByMobileNumber(String mobileNumber);

    Optional<AccountDTO> getById(UUID id);

    List<AccountDTO> getAccountsByIds(List<UUID> idsList);
}