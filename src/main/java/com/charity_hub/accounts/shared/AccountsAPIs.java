package com.charity_hub.accounts.shared;

import com.charity_hub.accounts.internal.shell.repositories.InvitationRepo;
import com.charity_hub.accounts.internal.shell.repositories.ReadAccountRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class AccountsAPIs implements IAccountsAPI {
    private final InvitationRepo invitationRepo;
    private final ReadAccountRepo readAccountRepo;
    private final DTOAccountMapper dtoAccountMapper;

    public AccountsAPIs(InvitationRepo invitationRepo, ReadAccountRepo readAccountRepo, DTOAccountMapper dtoAccountMapper) {
        this.invitationRepo = invitationRepo;
        this.readAccountRepo = readAccountRepo;
        this.dtoAccountMapper = dtoAccountMapper;
    }

    @Override
    public CompletableFuture<InvitationResponse> getInvitationByMobileNumber(String mobileNumber) {
        return invitationRepo.get(mobileNumber)
                .thenApply(invitation -> {
                    if (invitation == null) return null;
                    return new InvitationResponse(invitation.invitedMobileNumber().value(), invitation.inviterId());
                });
    }

    public CompletableFuture<AccountDTO> getById(UUID id) {
        return CompletableFuture.supplyAsync(() ->
                dtoAccountMapper.toDTO(readAccountRepo.getById(id).join())
        );
    }

    public CompletableFuture<List<AccountDTO>> getAccountsByIds(List<UUID> ids) {
        return CompletableFuture.supplyAsync(() ->
                readAccountRepo.getAccountsByIds(ids).join()
                        .stream()
                        .map(dtoAccountMapper::toDTO)
                        .collect(Collectors.toList())
        );
    }

    public List<AccountDTO> getAccountsByIdsTemp(List<UUID> ids) {
        return readAccountRepo.getAccountsByIdsTemp(ids)
                .stream()
                .map(dtoAccountMapper::toDTO)
                .collect(Collectors.toList());
    }
}