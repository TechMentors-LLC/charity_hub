package com.charity_hub.cases.internal.domain.events;

import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionId;

import java.util.UUID;

public record ContributionPaid(ContributionId id, UUID contributorId, int amount) implements CaseEvent {

    public static ContributionPaid from(Contribution contribution) {
        return new ContributionPaid(
                contribution.getId(),
                contribution.getContributorId(),
                contribution.getMoneyValue().value());
    }

}