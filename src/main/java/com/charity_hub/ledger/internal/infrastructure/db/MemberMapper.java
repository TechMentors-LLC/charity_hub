package com.charity_hub.ledger.internal.infrastructure.db;

import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;

import java.util.UUID;
import java.util.stream.Collectors;

public class MemberMapper {
    private MemberMapper() {
        // Private constructor to prevent instantiation of utility class
    }

    public static Member toDomain(MemberEntity entity) {
        return new Member(
                new MemberId(UUID.fromString(entity._id())),
                entity.parent() != null ? new MemberId(UUID.fromString(entity.parent())) : null,
                entity.ancestors().stream()
                        .map(UUID::fromString)
                        .map(MemberId::new)
                        .collect(Collectors.toList()),
                entity.children().stream()
                        .map(UUID::fromString)
                        .map(MemberId::new)
                        .collect(Collectors.toList()));
    }

    public static MemberEntity toDB(Member domain) {
        return new MemberEntity(
                domain.memberIdValue(),
                domain.ancestorsIds(),
                domain.parentId(),
                domain.childrenIds());
    }
}