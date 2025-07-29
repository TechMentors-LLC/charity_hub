package com.charity_hub.cases.internal.infrastructure.repositories.mappers;

import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class ContributionMapper {

    public ContributionEntity toDB(Contribution domain) {
        return new ContributionEntity(
                domain.getId().value().toString(),
                domain.getContributorId().toString(),
                domain.getCaseId().value(),
                domain.getMoneyValue().value(),
                getContributionStatusCode(domain.getContributionStatus()),
                domain.getContributionDate().getTime()
        );
    }

    public Contribution toDomain(ContributionEntity entity) {
        UUID id;
        try {
            // Try to parse the ID as a UUID
            id = UUID.fromString(entity._id());
        } catch (IllegalArgumentException e) {
            // If it's in the old format "ContributionId[value=...]" extract the UUID
            String idStr = entity._id();
            if (idStr.startsWith("ContributionId[value=") && idStr.endsWith("]")) {
                String uuidStr = idStr.substring("ContributionId[value=".length(), idStr.length() - 1);
                id = UUID.fromString(uuidStr);
            } else {
                // Generate a new ID as fallback
                id = UUID.randomUUID();
            }
        }
        
        return Contribution.create(
                id,
                UUID.fromString(entity.contributorId()),
                entity.caseCode(),
                entity.amount(),
                getContributionStatus(entity.status()),
                new Date(entity.contributionDate())
        );
    }

    private ContributionStatus getContributionStatus(int status) {
        return switch (status) {
            case ContributionEntity.STATUS_PLEDGED -> ContributionStatus.PLEDGED;
            case ContributionEntity.STATUS_PAID -> ContributionStatus.PAID;
            default -> ContributionStatus.CONFIRMED;
        };
    }

    public int getContributionStatusCode(ContributionStatus status) {
        return switch (status) {
            case PLEDGED -> ContributionEntity.STATUS_PLEDGED;
            case PAID -> ContributionEntity.STATUS_PAID;
            case CONFIRMED -> ContributionEntity.STATUS_CONFIRMED;
        };
    }
}