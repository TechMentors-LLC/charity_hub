package com.charity_hub.ledger.internal.infrastructure.repositories;

import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.infrastructure.db.MemberEntity;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.db.MemberMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@Repository
public class MembersNetworkRepo implements IMembersNetworkRepo {
    private final MongoCollection<MemberEntity> collection;

    public MembersNetworkRepo(MongoDatabase mongoDatabase) {
        this.collection = mongoDatabase.getCollection("connections", MemberEntity.class);
    }

    @Override
    public CompletableFuture<Member> getById(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            MemberEntity entity = collection.find(eq("_id", id.toString())).first();
            return entity != null ? MemberMapper.toDomain(entity) : null;
        });
    }

    @Override
    public CompletableFuture<Void> delete(MemberId id) {
        return CompletableFuture.runAsync(() -> {
            collection.deleteOne(eq("_id", id.value().toString()));
        });
    }

    @Override
    public CompletableFuture<Void> save(Member member) {
        return CompletableFuture.runAsync(() -> {
            MemberEntity entity = MemberMapper.toDB(member);
            collection.replaceOne(
                eq("_id", entity._id()),
                entity,
                new ReplaceOptions().upsert(true)
            );
        });
    }
}