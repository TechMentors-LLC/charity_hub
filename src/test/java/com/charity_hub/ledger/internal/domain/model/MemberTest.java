package com.charity_hub.ledger.internal.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Member Domain Model Tests")
class MemberTest {

    @Test
    @DisplayName("Should create new member with parent's ancestors")
    void shouldCreateNewMemberWithParentAncestors() {
        UUID parentId = UUID.randomUUID();
        UUID grandParentId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        Member parent = new Member(
                new MemberId(parentId),
                new MemberId(grandParentId),
                List.of(new MemberId(grandParentId)),
                Collections.emptyList()
        );

        Member newMember = Member.newMember(parent, newMemberId);

        assertThat(newMember.memberId().value()).isEqualTo(newMemberId);
        assertThat(newMember.parent().value()).isEqualTo(parentId);
        assertThat(newMember.ancestors()).hasSize(2);
        assertThat(newMember.ancestorsIds()).contains(grandParentId.toString(), parentId.toString());
    }

    @Test
    @DisplayName("Should return unmodifiable ancestors list")
    void shouldReturnUnmodifiableAncestorsList() {
        UUID memberId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Member member = new Member(
                new MemberId(memberId),
                new MemberId(parentId),
                List.of(new MemberId(parentId)),
                Collections.emptyList()
        );

        List<MemberId> ancestors = member.ancestors();

        assertThat(ancestors).isUnmodifiable();
    }

    @Test
    @DisplayName("Should return unmodifiable children list")
    void shouldReturnUnmodifiableChildrenList() {
        UUID memberId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Member member = new Member(
                new MemberId(memberId),
                new MemberId(parentId),
                Collections.emptyList(),
                List.of(new MemberId(childId))
        );

        List<MemberId> children = member.children();

        assertThat(children).isUnmodifiable();
    }

    @Test
    @DisplayName("Should return correct string IDs")
    void shouldReturnCorrectStringIds() {
        UUID memberId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Member member = new Member(
                new MemberId(memberId),
                new MemberId(parentId),
                List.of(new MemberId(parentId)),
                List.of(new MemberId(childId))
        );

        assertThat(member.memberIdValue()).isEqualTo(memberId.toString());
        assertThat(member.parentId()).isEqualTo(parentId.toString());
        assertThat(member.ancestorsIds()).containsExactly(parentId.toString());
        assertThat(member.childrenIds()).containsExactly(childId.toString());
    }
}
