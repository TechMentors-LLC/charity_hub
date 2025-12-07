package com.charity_hub.accounts.shared;

import com.charity_hub.accounts.internal.shell.repositories.InvitationRepo;
import com.charity_hub.accounts.internal.shell.repositories.ReadAccountRepo;
import org.springframework.stereotype.Component;

import java.util.List;
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
    public InvitationResponse getInvitationByMobileNumber(String mobileNumber) {
        var invitation = invitationRepo.get(mobileNumber);
        if (invitation == null) return null;
        return new InvitationResponse(invitation.invitedMobileNumber().value(), invitation.inviterId());
    }

    public AccountDTO getById(UUID id) {
        return dtoAccountMapper.toDTO(readAccountRepo.getById(id));
    }

    public List<AccountDTO> getAccountsByIds(List<UUID> ids) {
        return readAccountRepo.getAccountsByIds(ids)
                .stream()
                .map(dtoAccountMapper::toDTO)
                .collect(Collectors.toList());
    }
}