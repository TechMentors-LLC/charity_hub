package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.micrometer.observation.annotation.Observed;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository
public class ReadCaseRepo {
    private static final String CASES_COLLECTION = "cases";
    private static final String CONTRIBUTION_COLLECTION = "contributions";

    private final MongoCollection<CaseEntity> cases;
    private final MongoCollection<ContributionEntity> contributions;

    public ReadCaseRepo(MongoDatabase mongoDatabase) {
        this.cases = mongoDatabase.getCollection(CASES_COLLECTION, CaseEntity.class);
        this.contributions = mongoDatabase.getCollection(CONTRIBUTION_COLLECTION, ContributionEntity.class);
    }

    @Observed(name = "charity_hub.repo.read_case.get_by_code", contextualName = "read-case-repo-get-by-code")
    public CaseEntity getByCode(int code) {
        return cases.find(Filters.eq("code", code)).first();
    }

    @Observed(name = "charity_hub.repo.read_case.get_by_codes", contextualName = "read-case-repo-get-by-codes")
    public List<CaseEntity> getByCodes(List<Integer> codes) {
        return cases.find(Filters.in("code", codes))
                .into(new ArrayList<>());
    }

    @Observed(name = "charity_hub.repo.read_case.get_contributions", contextualName = "read-case-repo-get-contributions")
    public List<ContributionEntity> getContributionsByCaseCode(Long caseCode) {
        return contributions.find(Filters.eq("caseCode", caseCode))
                .into(new ArrayList<>());
    }

    @Observed(name = "charity_hub.repo.read_case.count", contextualName = "read-case-repo-count")
    public int getCasesCount(Supplier<Bson> filter) {
        Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
        if (filter != null) {
            query = Filters.and(query, filter.get());
        }
        return (int) cases.countDocuments(query);
    }

    @Observed(name = "charity_hub.repo.read_case.search", contextualName = "read-case-repo-search")
    public List<CaseEntity> search(
            int offset,
            int limit,
            Supplier<Bson> filter
    ) {
        Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
        if (filter != null) {
            query = Filters.and(query, filter.get());
        }

        return cases.find(query)
                .sort(Sorts.orderBy(
                        Sorts.ascending("status"),
                        Sorts.descending("lastUpdated")
                ))
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>());
    }

    @Observed(name = "charity_hub.repo.read_case.get_not_confirmed_contributions", contextualName = "read-case-repo-get-not-confirmed-contributions")
    public List<ContributionEntity> getNotConfirmedContributions(UUID contributorId) {
        return contributions.find(Filters.and(
                        Filters.eq("contributorId", contributorId.toString()),
                        Filters.ne("status", ContributionEntity.STATUS_CONFIRMED)
                ))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
    }

    public List<ContributionEntity> getContributions(List<UUID> contributorsIds) {
        return contributions.find(
                        Filters.in("contributorId",
                                contributorsIds.stream()
                                        .map(UUID::toString)
                                        .collect(Collectors.toList())
                        )
                )
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
    }

    public List<ContributionEntity> getContributions(UUID contributorId) {
        return contributions.find(Filters.eq("contributorId", contributorId.toString()))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
    }

    public List<CaseEntity> getDraftCases() {
        return cases.find(Filters.eq("status", CaseEntity.STATUS_DRAFT))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
    }
}