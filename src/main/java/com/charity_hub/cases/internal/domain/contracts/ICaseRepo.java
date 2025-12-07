package com.charity_hub.cases.internal.domain.contracts;

import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ICaseRepo {
    int nextCaseCodeTemp();
    CompletableFuture<Integer> nextCaseCode();
    
    Optional<Case> getByCodeTemp(CaseCode caseCode);
    CompletableFuture<Case> getByCode(CaseCode caseCode);

    void saveTemp(Case case_);
    CompletableFuture<Void> save(Case case_);

    void deleteTemp(CaseCode caseCode);
    CompletableFuture<Void> delete(CaseCode caseCode);
    
    void saveTemp(Contribution contribution);
    CompletableFuture<Void> save(Contribution contribution);

    Optional<Contribution> getContributionByIdTemp(UUID id);
    CompletableFuture<Contribution> getContributionById(UUID id);
}