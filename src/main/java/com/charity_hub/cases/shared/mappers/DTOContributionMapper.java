package com.charity_hub.cases.shared.mappers;

import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import org.springframework.stereotype.Component;

@Component
public class DTOContributionMapper {
    
    public ContributionDTO toDTO(ContributionEntity entity) {
        return new ContributionDTO(
            entity._id(),
            entity.contributorId(),
            entity.caseCode(),
            entity.amount(),
            entity.status(),
            entity.contributionDate(),
            entity.proofUrl()
        );
    }
}