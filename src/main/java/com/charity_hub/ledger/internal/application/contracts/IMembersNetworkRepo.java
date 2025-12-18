package com.charity_hub.ledger.internal.application.contracts;

import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;

import java.util.UUID;

public interface IMembersNetworkRepo {
    Member getById(UUID id);

    void delete(MemberId id);

    void save(Member member);

    boolean isParentOf(UUID parentId, UUID childId);
}
