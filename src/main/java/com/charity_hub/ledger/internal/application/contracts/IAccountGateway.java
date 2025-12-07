package com.charity_hub.ledger.internal.application.contracts;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.ledger.internal.application.models.InvitationResponse;
import com.charity_hub.ledger.internal.domain.model.MemberId;

import java.util.List;

public interface IAccountGateway {
    InvitationResponse getInvitationByMobileNumber(String mobileNumber);
    List<AccountDTO> getAccounts(List<MemberId> ids);
}