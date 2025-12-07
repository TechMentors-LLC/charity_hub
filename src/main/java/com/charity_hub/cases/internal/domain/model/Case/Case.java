package com.charity_hub.cases.internal.domain.model.Case;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.events.*;
import com.charity_hub.cases.internal.domain.exceptions.CaseExceptions.CannotContributeException;
import com.charity_hub.cases.internal.domain.exceptions.CaseExceptions.CannotDeleteCaseException;
import com.charity_hub.cases.internal.domain.model.CaseDetails.CaseDetails;
import com.charity_hub.cases.internal.domain.model.CaseDetails.Tag;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.Contributions;
import com.charity_hub.shared.domain.model.AggregateRoot;
import lombok.Getter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
public class Case extends AggregateRoot<CaseCode> {
    private final CaseCode caseCode;
    private CaseDetails details;
    private CaseStatus status;
    private CaseDocuments documents;
    private final Date creationDate;
    private Date lastUpdated;
    private final Contributions contributions;

    private Case(
            CaseCode caseCode,
            CaseDetails details,
            CaseStatus status,
            CaseDocuments documents,
            Contributions contributions,
            Date creationDate,
            Date lastUpdated
    ) {
        super(caseCode);
        this.caseCode = caseCode;
        this.details = details;
        this.status = status;
        this.documents = documents;
        this.contributions = contributions;
        this.creationDate = creationDate;
        this.lastUpdated = lastUpdated;
    }

    public static Case newCase(NewCaseProbs props) {
        Case case_ = create(CaseProps.from(props));

        if (case_.status.isOpened()) {
            case_.raiseEvent(CaseOpened.from(case_));
        }
        return case_;
    }

    public static Case create(CaseProps caseProps) {
        return new Case(
                new CaseCode(caseProps.code()),
                CaseDetails.of(
                        caseProps.title(),
                        caseProps.description(),
                        caseProps.goal(),
                        caseProps.acceptZakat(),
                        caseProps.tags()
                ),
                CaseStatus.of(caseProps.status()),
                CaseDocuments.of(caseProps.documents()),
                Contributions.of(caseProps.contributions()),
                caseProps.creationDate(),
                caseProps.lastUpdated()
        );
    }

    public void update(
            String title,
            String description,
            int goal,
            boolean acceptZakat,
            List<String> documentUrls
    ) {
        this.details = this.details.update(title, description, goal, acceptZakat);
        this.documents = this.documents.update(documentUrls);
        this.lastUpdated = new Date();
//        raiseEvent(new CaseUpdated(this));
    }

    public void delete(ICaseRepo caseRepo) {
        if (!status.isDraft()) {
            throw new CannotDeleteCaseException();
        }
        caseRepo.delete(getId());
    }

    public void open() {
        this.status = status.open();
        this.lastUpdated = new Date();
        raiseEvent(CaseOpened.from(this));
    }

    public void close() {
        this.status = status.close();
        this.lastUpdated = new Date();
        raiseEvent(CaseClosed.from(this));
    }

    public Contribution contribute(UUID contributorId, int amount) {
        if (!status.isOpened()) {
            throw new CannotContributeException();
        }

        var contribution = contributions.newContribution(contributorId, amount, caseCode);

        raiseEvent(ContributionMade.from(contribution));
        return contribution;
    }

    // Getters
    public int totalContributions() {
        return contributions.totalValue();
    }

    public int numberOfContributions() {
        return contributions.numberOfContributions();
    }

    public List<Tag> getTags() {
        return details.getTags();
    }

    public List<Document> getDocuments() {
        return documents.documents();
    }

    public List<Contribution> getContributions() {
        return contributions.values();
    }

    public String getTitle() {
        return details.getTitle();
    }

    public String getDescription() {
        return details.getDescription();
    }

    public int getGoal() {
        return details.getGoal();
    }

    public boolean getAcceptZakat() {
        return details.isAcceptZakat();
    }

    public Status getStatus() {
        return status.value();
    }
}