package com.charity_hub.accounts.shared;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IAccountsAPI {
    CompletableFuture<InvitationResponse> getInvitationByMobileNumber(String mobileNumber);

    CompletableFuture<AccountDTO> getById(UUID id);

    List<AccountDTO> getAccountsByIdsTemp(List<UUID> idsList);
    CompletableFuture<List<AccountDTO>> getAccountsByIds(List<UUID> idsList);
}