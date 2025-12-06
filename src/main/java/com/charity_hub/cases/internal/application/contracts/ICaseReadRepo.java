package com.charity_hub.cases.internal.application.contracts;

import com.charity_hub.cases.internal.application.queries.Case;
import com.charity_hub.cases.internal.application.queries.CaseCriteria;
import com.charity_hub.cases.internal.application.queries.Contribution;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ICaseReadRepo {
    Optional<Case> getByCode(int code);

    List<Case> getByCodes(List<Integer> codes);
    List<Contribution> getContributionsByCaseCode(int caseCode);


    int getCasesCount(CaseCriteria criteria);

    List<Case> search(int offset, int limit, CaseCriteria criteria);


    List<Case> getDraftCases();

    List<Contribution> getNotConfirmedContributions(UUID contributorId);

    List<Contribution> getContributions(List<UUID> contributorsIds);

    List<Contribution> getContributions(UUID contributorId);
}
