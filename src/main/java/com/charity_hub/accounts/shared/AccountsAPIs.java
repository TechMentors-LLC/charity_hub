package com.charity_hub.accounts.shared;

import com.charity_hub.accounts.internal.shell.repositories.InvitationRepo;
import com.charity_hub.accounts.internal.shell.repositories.ReadAccountRepo;

import io.micrometer.observation.annotation.Observed;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    @Observed(name = "AccountsAPIs.getInvitationByMobileNumber",contextualName = "get-invitation-by-mobile-number")
    public Optional<InvitationResponse> getInvitationByMobileNumber(String mobileNumber) {
   
        return invitationRepo.get(mobileNumber)
                .map(invitation -> new InvitationResponse(invitation.invitedMobileNumber().value(), invitation.inviterId()));
    }

    @Override
    @Observed(name = "AccountsAPIs.getById",contextualName = "get-account-by-id")
    public Optional<AccountDTO> getById(UUID id) {
        return readAccountRepo.getById(id)
                .map(dtoAccountMapper::toDTO);
    }

    @Override
    @Observed(name = "AccountsAPIs.getAccountsByIds",contextualName = "get-accounts-by-ids")
    public List<AccountDTO> getAccountsByIds(List<UUID> ids) {
               return readAccountRepo.getAccountsByIds(ids)
                        .stream()
                        .map(dtoAccountMapper::toDTO)
                        .collect(Collectors.toList());    
    }
}