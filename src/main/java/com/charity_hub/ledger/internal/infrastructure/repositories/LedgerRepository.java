package com.charity_hub.ledger.internal.infrastructure.repositories;

import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.LedgerId;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.db.LedgerEntity;
import com.charity_hub.ledger.internal.infrastructure.db.LedgerMapper;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * MongoDB implementation of ILedgerRepository.
 */
@Repository
public class LedgerRepository implements ILedgerRepository {

    private final MongoTemplate mongoTemplate;
    private final LedgerMapper mapper;
    private final IEventBus eventBus;

    public LedgerRepository(MongoTemplate mongoTemplate, LedgerMapper mapper, IEventBus eventBus) {
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;
        this.eventBus = eventBus;
    }

    @Override
    public Ledger findById(LedgerId id) {
        LedgerEntity entity = mongoTemplate.findById(id.value().toString(), LedgerEntity.class);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    public Ledger findByMemberId(MemberId memberId) {
        Query query = new Query(Criteria.where("memberId").is(memberId.value().toString()));
        LedgerEntity entity = mongoTemplate.findOne(query, LedgerEntity.class);
        return entity != null ? mapper.toDomain(entity) : null;
    }

    @Override
    @Transactional
    public void save(Ledger ledger) {
        LedgerEntity entity = mapper.toEntity(ledger);
        mongoTemplate.save(entity);

        // Publish domain events (occurredEvents() clears them internally)
        ledger.occurredEvents().forEach(eventBus::push);
    }

    @Override
    public boolean existsByMemberId(MemberId memberId) {
        Query query = new Query(Criteria.where("memberId").is(memberId.value().toString()));
        return mongoTemplate.exists(query, LedgerEntity.class);
    }
}
