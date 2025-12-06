package com.charity_hub.cases.shared;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICasesAPI {
    List<ContributionDTO> getUsersContributions(UUID userId);
    List<ContributionDTO> getNotConfirmedContributions(UUID userId);
    List<ContributionDTO> getUsersContributions(List<UUID> usersIds);
    List<CaseDTO> getCasesByCodes(List<Integer> casesCodes);

}