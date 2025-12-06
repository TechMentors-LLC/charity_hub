package com.charity_hub.cases.shared;

import com.charity_hub.cases.shared.mappers.DTOCaseMapper;
import com.charity_hub.cases.shared.mappers.DTOContributionMapper;
import com.charity_hub.cases.internal.infrastructure.repositories.ReadCaseRepo;
import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class CasesAPI implements ICasesAPI {
    private final ReadCaseRepo readCaseRepo;
    private final DTOContributionMapper contributionMapper;
    private final DTOCaseMapper caseMapper;

    public CasesAPI(ReadCaseRepo readCaseRepo, DTOContributionMapper contributionMapper, DTOCaseMapper caseMapper) {
        this.readCaseRepo = readCaseRepo;
        this.contributionMapper = contributionMapper;
        this.caseMapper = caseMapper;
    }

    public List<ContributionDTO> getUsersContributions(UUID userId) {
        var contributions = readCaseRepo.getContributions(userId);
        return contributions.stream()
                .map(contributionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ContributionDTO> getNotConfirmedContributions(UUID userId) {
        var contributions = readCaseRepo.getNotConfirmedContributions(userId);

        return contributions.stream()
                .map(contributionMapper::toDTO)
                .collect(Collectors.toList());

    }

    public List<ContributionDTO> getUsersContributions(List<UUID> usersIds) {
        var contributions = readCaseRepo.getContributions(usersIds);

        contributions.stream()
                .map(contributionMapper::toDTO)
                .collect(Collectors.toList());

    }

    public List<CaseDTO> getCasesByCodes(List<Integer> casesCodes) {
        var cases = readCaseRepo.getByCodes(casesCodes);

        return cases.stream()
                .map(caseMapper::toDTO)
                .collect(Collectors.toList());
    }
}