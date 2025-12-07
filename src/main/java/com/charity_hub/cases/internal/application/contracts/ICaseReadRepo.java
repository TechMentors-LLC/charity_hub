package com.charity_hub.cases.internal.application.contracts;

import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface ICaseReadRepo {
    CompletableFuture<CaseEntity> getByCode(int code);
    CaseEntity getByCodeTemp(int code);

    CompletableFuture<List<CaseEntity>> getByCodes(List<Integer> codes);

    CompletableFuture<List<ContributionEntity>> getContributionsByCaseCode(int caseCode);
    List<ContributionEntity> getContributionsByCaseCodeTemp(int caseCode);

    CompletableFuture<Integer> getCasesCount(Supplier<Bson> filter);
    int getCasesCountTemp(Supplier<Bson> filter);

    CompletableFuture<List<CaseEntity>> search(int offset, int limit, Supplier<Bson> filter);
    List<CaseEntity> searchTemp(int offset, int limit, Supplier<Bson> filter);

    CompletableFuture<List<ContributionEntity>> getNotConfirmedContributions(UUID contributorId);

    CompletableFuture<List<ContributionEntity>> getContributions(List<UUID> contributorsIds);

    CompletableFuture<List<ContributionEntity>> getContributions(UUID contributorId);

    CompletableFuture<List<CaseEntity>> getDraftCases();
    List<CaseEntity> getDraftCasesTemp();
}
