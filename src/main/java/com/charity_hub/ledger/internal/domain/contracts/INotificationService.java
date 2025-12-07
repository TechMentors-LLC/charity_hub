package com.charity_hub.ledger.internal.domain.contracts;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.model.Member;

public interface INotificationService {
    void notifyContributionPaid(ContributionPaidDTO contribution);

    void notifyContributionConfirmed(ContributionConfirmedDTO contribution);

    void notifyContributorToPay(ContributionRemindedDTO contribution);

    void notifyNewConnectionAdded(Member member);
}