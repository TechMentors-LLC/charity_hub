package com.charity_hub.ledger.internal.infrastructure.repositories;

import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.infrastructure.db.MemberEntity;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.db.MemberMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class MembersNetworkRepo implements IMembersNetworkRepo {
    private static final Logger logger = LoggerFactory.getLogger(MembersNetworkRepo.class);

    private final MongoCollection<MemberEntity> collection;

    public MembersNetworkRepo(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection("connections", MemberEntity.class);
    }

    @Override
    @Observed(name = "charity_hub.repo.member.get_by_id", contextualName = "member-repo-get-by-id")
    public Member getById(UUID id) {
        logger.debug("Looking up member by id: {}", id);
        MemberEntity entity = collection.find(eq("_id", id.toString())).first();
        if (entity == null) {
            logger.debug("Member not found with id: {}", id);
        }
        return entity != null ? MemberMapper.toDomain(entity) : null;
    }

    @Override
    @Observed(name = "charity_hub.repo.member.delete", contextualName = "member-repo-delete")
    public void delete(MemberId id) {
        logger.info("Deleting member with id: {}", id.value());
        collection.deleteOne(eq("_id", id.value().toString()));
        logger.debug("Member deleted successfully: {}", id.value());
    }

    @Override
    @Observed(name = "charity_hub.repo.member.save", contextualName = "member-repo-save")
    public void save(Member member) {
        logger.info("Saving member with id: {}", member.memberId().value());
        MemberEntity entity = MemberMapper.toDB(member);
        collection.replaceOne(
                eq("_id", entity._id()),
                entity,
                new ReplaceOptions().upsert(true));
        logger.debug("Member saved successfully: {}", member.memberId().value());
    }

    @Override
    @Observed(name = "charity_hub.repo.member.is_parent_of", contextualName = "member-repo-is-parent-of")
    public boolean isParentOf(UUID parentId, UUID childId) {
        Member child = getById(childId);
        if (child == null || child.parent() == null) {
            return false;
        }
        return child.parent().value().equals(parentId);
    }
}