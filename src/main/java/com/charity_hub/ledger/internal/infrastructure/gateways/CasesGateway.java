package com.charity_hub.ledger.internal.infrastructure.gateways;

import com.charity_hub.cases.shared.ICasesAPI;
import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.application.contracts.ICasesGateway;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class CasesGateway implements ICasesGateway {
    private final ICasesAPI casesAPI;

    public CasesGateway(ICasesAPI casesAPI) {
        this.casesAPI = casesAPI;
    }

    @Override
    public List<ContributionDTO> getContributions(UUID userId) {
        return casesAPI.getUsersContributions(userId);
    }

    @Override
    public List<CaseDTO> getCasesByIds(List<Integer> casesCodes) {
        return casesAPI.getCasesByCodes(casesCodes);
    }

    @Override
    public List<ContributionDTO> getNotConfirmedContributions(UUID userId) {
        return casesAPI.getNotConfirmedContributions(userId);
    }

    @Override
    public List<ContributionDTO> getContributions(List<UUID> usersIds) {
        return casesAPI.getUsersContributions(usersIds);
    }

}