package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.application.queries.Case;
import com.charity_hub.cases.internal.application.queries.CaseCriteria;
import com.charity_hub.cases.internal.application.queries.Contribution;
import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Repository
public class ReadCaseRepo implements ICaseReadRepo {
    private static final String CASES_COLLECTION = "cases";
    private static final String CONTRIBUTION_COLLECTION = "contributions";

    private final MongoCollection<CaseEntity> cases;
    private final MongoCollection<ContributionEntity> contributions;

    public ReadCaseRepo(MongoDatabase mongoDatabase) {
        this.cases = mongoDatabase.getCollection(CASES_COLLECTION, CaseEntity.class);
        this.contributions = mongoDatabase.getCollection(CONTRIBUTION_COLLECTION, ContributionEntity.class);
    }

    public Optional<Case> getByCode(int code) {
               CaseEntity entity = cases.find(Filters.eq("code", code)).first();
            return Optional.ofNullable(entity).map(this::toReadModel);
    }

    public List<Case> getByCodes(List<Integer> codes) {
        return cases.find(Filters.in("code", codes))
                .map(this::toReadModel)
                .into(new ArrayList<>());
    }

    public List<Contribution> getContributionsByCaseCode(int caseCode) {
                return contributions.find(Filters.eq("caseCode", caseCode))
                        .map(this::toContributionReadModel)
                        .into(new ArrayList<>());

    }

    public int getCasesCount(CaseCriteria criteria) {

            Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
            Bson criteriaFilter = buildFilter(criteria);

            if (criteriaFilter != null) {
                query = Filters.and(query, criteriaFilter);
            }
            return (int) cases.countDocuments(query);
    }

    public List<Case> search(
            int offset,
            int limit,
            CaseCriteria criteria
    ) {

        Bson query = Filters.ne("status", CaseEntity.STATUS_DRAFT);
        Bson criteriaFilter = buildFilter(criteria);
        if (criteriaFilter != null) {
            query = Filters.and(query, criteriaFilter);
        }

        return cases.find(query)
                .sort(Sorts.orderBy(
                        Sorts.ascending("status"),
                        Sorts.descending("lastUpdated")
                ))
                .skip(offset)
                .limit(limit)
                .map(this::toReadModel)
                .into(new ArrayList<>());

    }

    public List<Contribution> getNotConfirmedContributions(UUID contributorId) {
        return contributions.find(Filters.and(
                        Filters.eq("contributorId", contributorId.toString()),
                        Filters.ne("status", ContributionEntity.STATUS_CONFIRMED)
                ))
                .sort(Sorts.descending("lastUpdated"))
                .map(this::toContributionReadModel)
                .into(new ArrayList<>());
    }

    public List<Contribution> getContributions(List<UUID> contributorsIds) {
        return contributions.find(
                        Filters.in("contributorId",
                                contributorsIds.stream()
                                        .map(UUID::toString)
                                        .collect(Collectors.toList())
                        )
                )
                .sort(Sorts.descending("lastUpdated"))
                .map(this::toContributionReadModel)
                .into(new ArrayList<>());

    }

    public List<Contribution> getContributions(UUID contributorId) {
        return contributions.find(Filters.eq("contributorId", contributorId.toString()))
                .sort(Sorts.descending("lastUpdated"))
                .map(this::toContributionReadModel)
                .into(new ArrayList<>());
    }

    public List<Case> getDraftCases() {

              return  cases.find(Filters.eq("status", CaseEntity.STATUS_DRAFT))
                        .sort(Sorts.descending("lastUpdated"))
                      .map(this::toReadModel)
                        .into(new ArrayList<>());
    }

    private Contribution toContributionReadModel(ContributionEntity entity) {
        return new Contribution(
                entity._id(),
                UUID.fromString(entity.contributorId()),
                entity.caseCode(),
                entity.amount(),
                mapContributionStatus(entity.status()),
                new Date(entity.contributionDate()),
                entity.paymentProof()
        );
    }

    private Case toReadModel(CaseEntity entity) {
        return new Case(
                entity.code(),
                entity.title(),
                entity.description(),
                entity.goal(),
                entity.collected(),
                mapStatus(entity.status()),
                entity.tags(),
                entity.lastUpdated()
        );
    }
    private String mapStatus(int status) {
        return switch (status) {
            case CaseEntity.STATUS_DRAFT -> "DRAFT";
            case CaseEntity.STATUS_OPENED -> "OPENED";
            case CaseEntity.STATUS_CLOSED -> "CLOSED";
            default -> "UNKNOWN";
        };
    }

    private String mapContributionStatus(int status) {
        return switch (status) {
            case ContributionEntity.STATUS_PLEDGED -> "PLEDGED";
            case ContributionEntity.STATUS_PAID -> "PAID";
            case ContributionEntity.STATUS_CONFIRMED -> "CONFIRMED";
            default -> "UNKNOWN";
        };
    }
    private Bson buildFilter(CaseCriteria criteria) {
        if (criteria == null) return null;
        List<Bson> conditions = new ArrayList<>();

        if (criteria.code() != null) {
            conditions.add(Filters.eq("code", criteria.code()));
        }
        if (criteria.tag() != null) {
            conditions.add(Filters.in("tags", criteria.tag()));
        }
        if (criteria.content() != null && !criteria.content().isBlank()) {
            conditions.add(Filters.or(
                    Filters.regex("title", Pattern.compile(".*" + criteria.content() + ".*", Pattern.CASE_INSENSITIVE)),
                    Filters.regex("description", Pattern.compile(".*" + criteria.content() + ".*", Pattern.CASE_INSENSITIVE))
            ));
        }

        return conditions.isEmpty() ? null : Filters.and(conditions);
    }
}