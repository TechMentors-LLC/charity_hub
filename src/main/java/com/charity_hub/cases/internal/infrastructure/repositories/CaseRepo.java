package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.events.CaseEvent;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.CaseEventsMapper;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.CaseMapper;
import com.charity_hub.cases.internal.infrastructure.repositories.mappers.ContributionMapper;
import com.charity_hub.shared.domain.IEventBus;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<Integer> nextCaseCode() {
        CaseEntity lastCase = cases.find()
                .sort(new org.bson.Document("code", -1))
                .limit(1)
                .first();
        int nextCode = (lastCase != null ? lastCase.code() : 20039) + 1;
        return Optional.of(nextCode);
    }

    @Override
    public Optional<Case> getByCode(CaseCode caseCode) {
        CaseEntity entity = cases.find(new org.bson.Document("code", caseCode.value()))
                .first();
        if (entity == null) return Optional.empty();
        return Optional.of(caseMapper.toDomain(
                entity,
                getContributionsByCaseCode(new CaseCode(entity.code()))
        ));
    }

    @Override
    public void save(Case case_) {
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

    }

    @Override
    public void delete(CaseCode caseCode) {
        cases.deleteOne(new org.bson.Document("code", caseCode.value()));
    }

    private List<ContributionEntity> getContributionsByCaseCode(CaseCode caseCode) {
        return contributions.find(new org.bson.Document("caseCode", caseCode.value()))
                .into(new ArrayList<>());
    }

    @Override
    public void save(Contribution contribution) {
        // Check if this is a status change (pay or confirm operation)
        if (contribution.getContributionStatus() == ContributionStatus.PAID ||
                contribution.getContributionStatus() == ContributionStatus.CONFIRMED) {
            // Update status and paymentProof if provided
            var updates = new ArrayList<org.bson.conversions.Bson>();
            updates.add(Updates.set("status", contributionMapper.getContributionStatusCode(contribution.getContributionStatus())));

            // If paymentProof is provided, update it as well
            if (contribution.getPaymentProof() != null) {
                updates.add(Updates.set("paymentProof", contribution.getPaymentProof()));
            }

            var updateResult = contributions.updateOne(
                    new org.bson.Document("_id", contribution.getId().value().toString()),
                    Updates.combine(updates),
                    new UpdateOptions().upsert(false)
            );

            // Check if the update modified any documents
            if (updateResult.getModifiedCount() == 0 && updateResult.getMatchedCount() == 0) {
                throw new IllegalStateException("Failed to update contribution status: Contribution does not exist.");
            }
        } else {
            // For other operations, replace the entire document
            contributions.replaceOne(
                    new org.bson.Document("_id", contribution.getId().value().toString()),
                    contributionMapper.toDB(contribution),
                    new ReplaceOptions().upsert(true)
            );
        }

        contribution.occurredEvents()
                .forEach(eventBus::push);
    }

    @Override
    public Optional<Contribution> getContributionById(UUID id) {
        ContributionEntity entity = contributions.find(
                new org.bson.Document("_id", id.toString())
        ).first();

        return Optional.ofNullable(entity).map(contributionMapper::toDomain);
    }
}