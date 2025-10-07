package com.charity_hub.cases.internal.domain.model.Contribution;

import com.charity_hub.cases.internal.domain.events.ContributionConfirmed;
import com.charity_hub.cases.internal.domain.events.ContributionPaid;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.domain.model.AggregateRoot;
import com.charity_hub.shared.exceptions.BusinessRuleException;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

@Getter
public class Contribution extends AggregateRoot<ContributionId> {
    private final UUID contributorId;
    private final CaseCode caseId;
    private ContributionStatus contributionStatus;
    private final MoneyValue moneyValue;
    private final Date contributionDate;
    private String proofUrl; // Optional proof URL for payment

    public Contribution(
            ContributionId id,
            UUID contributorId,
            CaseCode caseCode,
            MoneyValue moneyValue,
            ContributionStatus contributionStatus,
            Date contributionDate,
            String proofUrl
    ) {
        super(id);
        this.contributorId = contributorId;
        this.caseId = caseCode;
        this.moneyValue = moneyValue;
        this.contributionStatus = contributionStatus;
        this.contributionDate = contributionDate;
        this.proofUrl = proofUrl;
    }

    public static Contribution new_(
            UUID contributorId,
            int caseCode,
            int amount
    ) {
        return new Contribution(
                ContributionId.generate(),
                contributorId,
                new CaseCode(caseCode),
                MoneyValue.of(amount),
                ContributionStatus.PLEDGED,
                new Date(),
                null // No proof URL for new contributions
        );
    }

    public static Contribution create(
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate
    ) {
        return create(
                UUID.randomUUID(),
                contributorId,
                caseCode,
                amount,
                contributionStatus,
                contributionDate,
                null // Default to no proof URL
        );
    }

    public static Contribution create(
            UUID id,
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate
    ) {
        return create(
                id,
                contributorId,
                caseCode,
                amount,
                contributionStatus,
                contributionDate,
                null // Default to no proof URL
        );
    }

    public static Contribution create(
            UUID id,
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate,
            String proofUrl
    ) {
        return new Contribution(
                new ContributionId(id),
                contributorId,
                new CaseCode(caseCode),
                MoneyValue.of(amount),
                contributionStatus,
                contributionDate != null ? contributionDate : new Date(),
                proofUrl
        );
    }

    public void pay(String proofUrl) {
        if (contributionStatus.isNotPledged()) {
            throw new BusinessRuleException("The Contribution has been paid already");
        }
        contributionStatus = ContributionStatus.PAID;
        this.proofUrl = proofUrl; // Set proof URL if provided
        raiseEvent(ContributionPaid.from(this));
    }

    public void confirm() {
        if (contributionStatus.isConfirmed()) {
            throw new BusinessRuleException("The Contribution is already confirmed");
        }
        if (contributionStatus != ContributionStatus.PAID) {
            throw new BusinessRuleException("The Contribution must be paid before it can be confirmed");
        }
        contributionStatus = ContributionStatus.CONFIRMED;
        raiseEvent(ContributionConfirmed.from(this));
    }

    public String contributionId() {
        return getId().value().toString();
    }
}