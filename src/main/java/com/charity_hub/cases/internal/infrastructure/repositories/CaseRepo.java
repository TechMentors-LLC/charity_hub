package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.events.CaseEvent;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.CaseEventsMapper;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.CaseMapper;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.ContributionMapper;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.charity_hub.shared.domain.IEventBus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Repository
public class CaseRepo implements ICaseRepo {
    private static final String CASES_COLLECTION = "cases";
    private static final String CONTRIBUTION_COLLECTION = "contributions";

    private final MongoCollection<CaseEntity> cases;
    private final MongoCollection<ContributionEntity> contributions;
    private final IEventBus eventBus;
    private final CaseMapper caseMapper;
    private final ContributionMapper contributionMapper;

    public CaseRepo(
            MongoDatabase mongoDatabase,
            IEventBus eventBus,
            CaseMapper caseMapper,
            ContributionMapper contributionMapper
    ) {
        this.cases = mongoDatabase.getCollection(CASES_COLLECTION, CaseEntity.class);
        this.contributions = mongoDatabase.getCollection(CONTRIBUTION_COLLECTION, ContributionEntity.class);
        this.eventBus = eventBus;
        this.caseMapper = caseMapper;
        this.contributionMapper = contributionMapper;
    }

    @Override
    public CompletableFuture<Integer> nextCaseCode() {
        return CompletableFuture.supplyAsync(() -> {
            CaseEntity lastCase = cases.find()
                    .sort(new org.bson.Document("code", -1))
                    .limit(1)
                    .first();
            return (lastCase != null ? lastCase.code() : 20039) + 1;
        });
    }

    @Override
    public CompletableFuture<Case> getByCode(CaseCode caseCode) {
        return CompletableFuture.supplyAsync(() -> {
            CaseEntity entity = cases.find(new org.bson.Document("code", caseCode.value()))
                    .first();
            if (entity == null) return null;
            return caseMapper.toDomain(
                entity, 
                getContributionsByCaseCode(new CaseCode(entity.code()))
            );
        });
    }

    @Override
    public CompletableFuture<Void> save(Case case_) {
        return CompletableFuture.runAsync(() -> {
            List<Contribution> caseContributions = case_.getContributions();
            if (!caseContributions.isEmpty()) {
                List<com.mongodb.client.model.ReplaceOneModel<ContributionEntity>> updates = 
                    caseContributions.stream()
                        .map(contribution -> new com.mongodb.client.model.ReplaceOneModel<>(
                            new org.bson.Document("_id", contribution.getId().value().toString()),
                            contributionMapper.toDB(contribution),
                            new ReplaceOptions().upsert(true)
                        ))
                        .collect(Collectors.toList());
                
                contributions.bulkWrite(updates);
            }

            cases.replaceOne(
                new org.bson.Document("code", case_.getCaseCode().value()),
                caseMapper.toDB(case_),
                new ReplaceOptions().upsert(true)
            );

            case_.occurredEvents().stream()
                .map(event -> CaseEventsMapper.map((CaseEvent) event))
                .forEach(eventBus::push);
        });
    }

    @Override
    public CompletableFuture<Void> delete(CaseCode caseCode) {
        return CompletableFuture.runAsync(() -> 
            cases.deleteOne(new org.bson.Document("code", caseCode.value()))
        );
    }

    private List<ContributionEntity> getContributionsByCaseCode(CaseCode caseCode) {
        return contributions.find(new org.bson.Document("caseCode", caseCode.value()))
                .into(new ArrayList<>());
    }

    @Override
    public CompletableFuture<Void> save(Contribution contribution) {
        return CompletableFuture.runAsync(() -> {
            contributions.replaceOne(
                new org.bson.Document("_id", contribution.getId().value().toString()),
                contributionMapper.toDB(contribution),
                new ReplaceOptions().upsert(true)
            );
            
            contribution.occurredEvents()
                .forEach(eventBus::push);
        });
    }

    @Override
    public CompletableFuture<Contribution> getContributionById(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            ContributionEntity entity = contributions.find(
                new org.bson.Document("_id", id.toString())
            ).first();
            
            return entity != null ? contributionMapper.toDomain(entity) : null;
        });
    }
}