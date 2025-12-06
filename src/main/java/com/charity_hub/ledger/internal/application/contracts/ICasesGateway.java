package com.charity_hub.ledger.internal.application.contracts;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICasesGateway {
    List<ContributionDTO> getContributions(UUID userId);

    List<CaseDTO> getCasesByIds(List<Integer> casesCodes);

    List<ContributionDTO> getNotConfirmedContributions(UUID userId);

    List<ContributionDTO> getContributions(List<UUID> usersIds);
}
