package com.charity_hub.cases.internal.domain.model.Contribution;

import com.charity_hub.cases.internal.domain.events.ContributionConfirmed;
import com.charity_hub.cases.internal.domain.events.ContributionMade;
import com.charity_hub.cases.internal.domain.events.ContributionPaid;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.domain.model.AggregateRoot;
import com.charity_hub.shared.exceptions.BusinessRuleException;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.UUID;

@Getter
public class Contribution extends AggregateRoot<ContributionId> {
    private final UUID contributorId;
    private final CaseCode caseId;
    private ContributionStatus contributionStatus;
    private final MoneyValue moneyValue;
    private final Date contributionDate;
    private String paymentProof;

    public Contribution(
            ContributionId id,
            UUID contributorId,
            CaseCode caseCode,
            MoneyValue moneyValue,
            ContributionStatus contributionStatus,
            Date contributionDate,
            String paymentProof) {
        super(id);
        this.contributorId = contributorId;
        this.caseId = caseCode;
        this.moneyValue = moneyValue;
        this.contributionStatus = contributionStatus;
        this.contributionDate = contributionDate;
        this.paymentProof = paymentProof;
    }

    public static Contribution new_(
            UUID contributorId,
            int caseCode,
            int amount) {
        Contribution contribution = new Contribution(
                ContributionId.generate(),
                contributorId,
                new CaseCode(caseCode),
                MoneyValue.of(amount),
                ContributionStatus.PLEDGED,
                new Date(),
                null);
        contribution.raiseEvent(ContributionMade.from(contribution));
        return contribution;
    }

    public static Contribution create(
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate) {
        return create(
                UUID.randomUUID(),
                contributorId,
                caseCode,
                amount,
                contributionStatus,
                contributionDate,
                null);
    }

    public static Contribution create(
            UUID id,
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate) {
        return create(
                id,
                contributorId,
                caseCode,
                amount,
                contributionStatus,
                contributionDate,
                null);
    }

    public static Contribution create(
            UUID id,
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate,
            String paymentProof) {
        // This method is used by the Mapper for reconstitution.
        // We generally shouldn't raise events here for reconstitution.
        // If we need to create a new contribution with a specific ID, we should use a
        // specific method.
        // Assuming this creates a NEW one... but Mapper uses it.
        // The safest fix is to remove the event raising from here OR provide a separate
        // reconstitute method.
        // Since I'm adding `reconstitute` below, I will keep this as is or deprecate
        // it?
        // Actually, looking at usages, this method with `if (PLEDGED)` was recently
        // added.
        // I will change this to NOT raise events, as `new_` is for new contributions.
        return new Contribution(
                new ContributionId(id),
                contributorId,
                new CaseCode(caseCode),
                MoneyValue.of(amount),
                contributionStatus,
                contributionDate != null ? contributionDate : new Date(),
                paymentProof);
    }

    public static Contribution reconstitute(
            UUID id,
            UUID contributorId,
            int caseCode,
            int amount,
            ContributionStatus contributionStatus,
            Date contributionDate,
            String paymentProof) {
        return new Contribution(
                new ContributionId(id),
                contributorId,
                new CaseCode(caseCode),
                MoneyValue.of(amount),
                contributionStatus,
                contributionDate != null ? contributionDate : new Date(),
                paymentProof);
    }

    /**
     * @param paymentProof Optional. The payment proof document or reference, may be
     *                     null if not provided.
     */

    public void pay(@Nullable String paymentProof) {
        if (contributionStatus.isNotPledged()) {
            throw new BusinessRuleException("The Contribution has been paid already");
        }
        contributionStatus = ContributionStatus.PAID;
        this.paymentProof = paymentProof;
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