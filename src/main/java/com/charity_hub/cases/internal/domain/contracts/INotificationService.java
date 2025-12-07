package com.charity_hub.cases.internal.domain.contracts;

import com.charity_hub.cases.shared.dtos.CaseClosedDTO;
import com.charity_hub.cases.shared.dtos.CaseOpenedDTO;
import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;

public interface INotificationService {
    void subscribeAccountToCaseUpdates(String token);
    
    void notifyCaseOpened(CaseOpenedDTO case_);
    
    void notifyCaseClosed(CaseClosedDTO case_);
    
    void notifyContributionMade(ContributionMadeDTO contribution);
}