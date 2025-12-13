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
                Collections.emptyList());

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
                Collections.emptyList());

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
                List.of(new MemberId(childId)));

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
                List.of(new MemberId(childId)));

        assertThat(member.memberIdValue()).isEqualTo(memberId.toString());
        assertThat(member.parentId()).isEqualTo(parentId.toString());
        assertThat(member.ancestorsIds()).containsExactly(parentId.toString());
        assertThat(member.childrenIds()).containsExactly(childId.toString());
    }

    @Test
    @DisplayName("Should create root member without parent or ancestors")
    void shouldCreateRootMemberWithoutParent() {
        UUID memberId = UUID.randomUUID();

        Member root = Member.newRootMember(memberId);

        assertThat(root.memberId().value()).isEqualTo(memberId);
        assertThat(root.parent()).isNull();
        assertThat(root.ancestors()).isEmpty();
        assertThat(root.children()).isEmpty();
    }

    @Test
    @DisplayName("Should return null parentId for root member")
    void shouldReturnNullParentIdForRoot() {
        UUID memberId = UUID.randomUUID();

        Member root = Member.newRootMember(memberId);

        assertThat(root.parentId()).isNull();
    }

    @Test
    @DisplayName("Should add child and return new member instance")
    void shouldAddChildAndReturnNewInstance() {
        UUID parentId = UUID.randomUUID();
        UUID childId = UUID.randomUUID();

        Member parent = Member.newRootMember(parentId);
        Member updatedParent = parent.addChild(new MemberId(childId));

        // Original parent should remain unchanged
        assertThat(parent.children()).isEmpty();

        // Updated parent should have the child
        assertThat(updatedParent.children()).hasSize(1);
        assertThat(updatedParent.children().get(0).value()).isEqualTo(childId);
    }

    @Test
    @DisplayName("Should preserve existing children when adding new child")
    void shouldPreserveExistingChildren() {
        UUID parentId = UUID.randomUUID();
        UUID child1Id = UUID.randomUUID();
        UUID child2Id = UUID.randomUUID();

        Member parent = new Member(
                new MemberId(parentId),
                null,
                Collections.emptyList(),
                List.of(new MemberId(child1Id)));

        Member updatedParent = parent.addChild(new MemberId(child2Id));

        assertThat(updatedParent.children()).hasSize(2);
        assertThat(updatedParent.childrenIds()).containsExactly(
                child1Id.toString(), child2Id.toString());
    }
}
