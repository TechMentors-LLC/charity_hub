package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository
public class CaseReadRepo implements ICaseReadRepo {
    private static final Logger logger = LoggerFactory.getLogger(CaseReadRepo.class);
    private static final String CASES_COLLECTION = "cases";
    private static final String CONTRIBUTION_COLLECTION = "contributions";

    private final MongoCollection<CaseEntity> cases;
    private final MongoCollection<ContributionEntity> contributions;

    public CaseReadRepo(MongoDatabase mongoDatabase) {
        this.cases = mongoDatabase.getCollection(CASES_COLLECTION, CaseEntity.class);
        this.contributions = mongoDatabase.getCollection(CONTRIBUTION_COLLECTION, ContributionEntity.class);
    }

    @Override
    @io.micrometer.core.annotation.Timed(value = "charity_hub.repo.case_read.get_by_code", description = "Time taken to fetch case by code")
    public CaseEntity getByCode(int code) {
        logger.debug("Looking up case by code: {}", code);
        return cases.find(Filters.eq("code", code)).first();
    }

    @Override
    @io.micrometer.core.annotation.Timed(value = "charity_hub.repo.case_read.get_by_codes", description = "Time taken to fetch cases by codes")
    public List<CaseEntity> getByCodes(List<Integer> codes) {
        logger.debug("Looking up cases by codes: {}", codes);
        List<CaseEntity> result = cases.find(Filters.in("code", codes))
                .into(new ArrayList<>());
        logger.debug("Found {} cases for {} codes", result.size(), codes.size());
        return result;
    }

    @Override
    @io.micrometer.core.annotation.Timed(value = "charity_hub.repo.case_read.get_contributions", description = "Time taken to fetch contributions by case code")
    public List<ContributionEntity> getContributionsByCaseCode(int caseCode) {
        logger.debug("Looking up contributions for case code: {}", caseCode);
        List<ContributionEntity> result = contributions.find(Filters.eq("caseCode", caseCode))
                .into(new ArrayList<>());
        logger.debug("Found {} contributions for case code: {}", result.size(), caseCode);
        return result;
    }

    @Override
    @io.micrometer.core.annotation.Timed(value = "charity_hub.repo.case_read.count", description = "Time taken to count cases")
    public int getCasesCount(Supplier<Bson> filter) {
        logger.debug("Counting cases with filter");
        Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
        if (filter != null) {
            query = Filters.and(query, filter.get());
        }
        int count = (int) cases.countDocuments(query);
        logger.debug("Cases count: {}", count);
        return count;
    }

    @Override
    @io.micrometer.core.annotation.Timed(value = "charity_hub.repo.case_read.search", description = "Time taken to search cases")
    public List<CaseEntity> search(
            int offset,
            int limit,
            Supplier<Bson> filter
    ) {
        logger.debug("Searching cases with offset: {}, limit: {}", offset, limit);
        Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
        if (filter != null) {
            query = Filters.and(query, filter.get());
        }

        List<CaseEntity> result = cases.find(query)
                .sort(Sorts.orderBy(
                        Sorts.ascending("status"),
                        Sorts.descending("lastUpdated")
                ))
                .skip(offset)
                .limit(limit)
                .into(new ArrayList<>());
        logger.debug("Search returned {} cases", result.size());
        return result;
    }

    @Override
    public List<ContributionEntity> getNotConfirmedContributions(UUID contributorId) {
        logger.debug("Looking up not confirmed contributions for contributor: {}", contributorId);
        List<ContributionEntity> result = contributions.find(Filters.and(
                        Filters.eq("contributorId", contributorId.toString()),
                        Filters.ne("status", ContributionEntity.STATUS_CONFIRMED)
                ))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
        logger.debug("Found {} not confirmed contributions for contributor: {}", result.size(), contributorId);
        return result;
    }

    @Override
    public List<ContributionEntity> getContributions(List<UUID> contributorsIds) {
        logger.debug("Looking up contributions for {} contributors", contributorsIds.size());
        List<ContributionEntity> result = contributions.find(
                        Filters.in("contributorId",
                                contributorsIds.stream()
                                        .map(UUID::toString)
                                        .collect(Collectors.toList())
                        )
                )
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
        logger.debug("Found {} contributions for {} contributors", result.size(), contributorsIds.size());
        return result;
    }

    @Override
    public List<ContributionEntity> getContributions(UUID contributorId) {
        logger.debug("Looking up contributions for contributor: {}", contributorId);
        List<ContributionEntity> result = contributions.find(Filters.eq("contributorId", contributorId.toString()))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
        logger.debug("Found {} contributions for contributor: {}", result.size(), contributorId);
        return result;
    }

    @Override
    public List<CaseEntity> getDraftCases() {
        logger.debug("Looking up draft cases");
        List<CaseEntity> result = cases.find(Filters.eq("status", CaseEntity.STATUS_DRAFT))
                .sort(Sorts.descending("lastUpdated"))
                .into(new ArrayList<>());
        logger.debug("Found {} draft cases", result.size());
        return result;
    }
}