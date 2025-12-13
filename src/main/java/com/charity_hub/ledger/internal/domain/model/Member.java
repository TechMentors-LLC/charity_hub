package com.charity_hub.ledger.internal.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record Member(MemberId memberId, MemberId parent, List<MemberId> ancestors, List<MemberId> children) {

    public static Member newMember(Member parent, UUID memberId) {
        List<MemberId> newAncestors = new ArrayList<>(parent.ancestors());
        newAncestors.add(parent.memberId());

        return new Member(
                new MemberId(memberId),
                parent.memberId(),
                newAncestors,
                Collections.emptyList());
    }

    public static Member newRootMember(UUID memberId) {
        return new Member(
                new MemberId(memberId),
                null, // Root has no parent
                Collections.emptyList(), // Root has no ancestors
                Collections.emptyList());
    }

    public List<String> ancestorsIds() {
        return ancestors().stream()
                .map(memberId -> memberId.value().toString())
                .toList();
    }

    public String parentId() {
        return parent() != null ? parent().value().toString() : null;
    }

    public List<String> childrenIds() {
        return children().stream()
                .map(memberId -> memberId.value().toString())
                .toList();
    }

    public String memberIdValue() {
        return memberId().value().toString();
    }

    @Override
    public List<MemberId> ancestors() {
        return Collections.unmodifiableList(ancestors);
    }

    @Override
    public List<MemberId> children() {
        return Collections.unmodifiableList(children);
    }

    public Member addChild(MemberId childId) {
        List<MemberId> newChildren = new ArrayList<>(children);
        newChildren.add(childId);
        return new Member(
                memberId(),
                parent(),
                ancestors(),
                Collections.unmodifiableList(newChildren));
    }
}