package com.charity_hub.cases.internal.application.contracts;

import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface ICaseReadRepo {
    CaseEntity getByCode(int code);

    List<CaseEntity> getByCodes(List<Integer> codes);

    List<ContributionEntity> getContributionsByCaseCode(int caseCode);

    int getCasesCount(Supplier<Bson> filter);

    List<CaseEntity> search(int offset, int limit, Supplier<Bson> filter);

    List<ContributionEntity> getNotConfirmedContributions(UUID contributorId);

    List<ContributionEntity> getContributions(List<UUID> contributorsIds);

    List<ContributionEntity> getContributions(UUID contributorId);

    List<CaseEntity> getDraftCases();
}
