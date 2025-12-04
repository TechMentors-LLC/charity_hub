package com.charity_hub.cases.internal.domain.contracts;

import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;

import java.util.Optional;
import java.util.UUID;

public interface ICaseRepo {
    Optional<Integer> nextCaseCode();
    
    Optional<Case> getByCode(CaseCode caseCode);

    void save(Case case_);

    void delete(CaseCode caseCode);

    void save(Contribution contribution);

    Optional<Contribution> getContributionById(UUID id);
}