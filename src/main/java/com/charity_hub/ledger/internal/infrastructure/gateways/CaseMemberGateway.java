package com.charity_hub.ledger.internal.infrastructure.gateways;

import com.charity_hub.cases.shared.IMemberGateway;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CaseMemberGateway implements IMemberGateway {

    private final MembersNetworkRepo membersNetworkRepo;

    public CaseMemberGateway(MembersNetworkRepo membersNetworkRepo) {
        this.membersNetworkRepo = membersNetworkRepo;
    }

    @Override
    public boolean isParent(UUID parentId, UUID childId) {
        Member childMember = membersNetworkRepo.getById(childId);
        if (childMember == null) {
            return false;
        }

        return childMember.ancestors().contains(new MemberId(parentId));
    }
}
