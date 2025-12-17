package com.charity_hub.ledger.internal.infrastructure.gateways;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.accounts.shared.IAccountsAPI;
import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.application.models.InvitationResponse;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("ledgerAccountsGateway")
public class AccountsGateway implements IAccountGateway {
    private final IAccountsAPI accountsAPI;

    public AccountsGateway(IAccountsAPI accountsAPI) {
        this.accountsAPI = accountsAPI;
    }

    @Override
    public InvitationResponse getInvitationByMobileNumber(String mobileNumber) {
        var account = accountsAPI.getInvitationByMobileNumber(mobileNumber);
        if (account == null) {
            return null;
        }
        return new InvitationResponse(
                account.invitedMobileNumber(),
                account.inviterId());
    }

    @Override
    public List<AccountDTO> getAccounts(List<MemberId> ids) {
        return accountsAPI.getAccountsByIds(ids.stream().map(MemberId::value).toList());
    }
}