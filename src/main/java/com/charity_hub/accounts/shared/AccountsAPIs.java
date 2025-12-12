package com.charity_hub.accounts.shared;

import com.charity_hub.accounts.internal.application.contracts.IAccountReadRepo;
import com.charity_hub.accounts.internal.application.contracts.IInvitationRepo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountsAPIs implements IAccountsAPI {
    private final IInvitationRepo invitationRepo;
    private final IAccountReadRepo accountReadRepo;

    public AccountsAPIs(IInvitationRepo invitationRepo, IAccountReadRepo accountReadRepo) {
        this.invitationRepo = invitationRepo;
        this.accountReadRepo = accountReadRepo;
    }

    @Override
    public InvitationResponse getInvitationByMobileNumber(String mobileNumber) {
        var invitation = invitationRepo.get(mobileNumber);
        if (invitation == null)
            return null;
        return new InvitationResponse(invitation.invitedMobileNumber().value(), invitation.inviterId());
    }

    @Override
    public AccountDTO getById(UUID id) {
        return accountReadRepo.getById(id);
    }

    @Override
    public List<AccountDTO> getAccountsByIds(List<UUID> ids) {
        return accountReadRepo.getAccountsByIds(ids);
    }
}