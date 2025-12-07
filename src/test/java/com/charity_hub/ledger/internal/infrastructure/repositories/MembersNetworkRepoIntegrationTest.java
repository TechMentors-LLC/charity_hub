package com.charity_hub.ledger.internal.infrastructure.repositories;

import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MembersNetworkRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class MembersNetworkRepoIntegrationTest {

    private static final MongoDBContainer mongoDBContainer;

    static {
        mongoDBContainer = new MongoDBContainer("mongo:7.0")
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String mongoUri = mongoDBContainer.getReplicaSetUrl() + "?serverSelectionTimeoutMS=1000&connectTimeoutMS=1000&socketTimeoutMS=1000";
        registry.add("spring.data.mongodb.uri", () -> mongoUri);
    }

    @Autowired
    private IMembersNetworkRepo membersNetworkRepo;

    @Nested
    @DisplayName("Member CRUD Operations")
    class MemberCrudOperations {

        @Test
        @DisplayName("Should save and retrieve member by ID")
        void shouldSaveAndRetrieveMemberById() {
            UUID memberId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            Member member = new Member(
                    new MemberId(memberId),
                    new MemberId(parentId),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            membersNetworkRepo.save(member);
            Member retrieved = membersNetworkRepo.getById(memberId);

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.memberId().value()).isEqualTo(memberId);
            assertThat(retrieved.parent().value()).isEqualTo(parentId);
        }

        @Test
        @DisplayName("Should return null when member not found")
        void shouldReturnNullWhenMemberNotFound() {
            Member retrieved = membersNetworkRepo.getById(UUID.randomUUID());

            assertThat(retrieved).isNull();
        }

        @Test
        @DisplayName("Should save member and retrieve it")
        void shouldSaveMemberAndRetrieveIt() {
            UUID memberId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            UUID child1Id = UUID.randomUUID();
            UUID child2Id = UUID.randomUUID();
            
            Member member = new Member(
                    new MemberId(memberId),
                    new MemberId(parentId),
                    Collections.emptyList(),
                    List.of(new MemberId(child1Id), new MemberId(child2Id))
            );

            membersNetworkRepo.save(member);
            Member retrieved = membersNetworkRepo.getById(memberId);

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.memberId().value()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("Should delete member")
        void shouldDeleteMember() {
            UUID memberId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            Member member = new Member(
                    new MemberId(memberId),
                    new MemberId(parentId),
                    Collections.emptyList(),
                    Collections.emptyList()
            );

            membersNetworkRepo.save(member);
            assertThat(membersNetworkRepo.getById(memberId)).isNotNull();

            membersNetworkRepo.delete(new MemberId(memberId));
            assertThat(membersNetworkRepo.getById(memberId)).isNull();
        }

        @Test
        @DisplayName("Should update existing member by saving again")
        void shouldUpdateExistingMemberBySavingAgain() {
            UUID memberId = UUID.randomUUID();
            UUID parentId = UUID.randomUUID();
            
            // Save initial member
            Member member = new Member(
                    new MemberId(memberId),
                    new MemberId(parentId),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            membersNetworkRepo.save(member);
            
            // Verify initial save
            Member initial = membersNetworkRepo.getById(memberId);
            assertThat(initial).isNotNull();

            // Save again (update) - should not throw
            membersNetworkRepo.save(member);
            
            Member afterUpdate = membersNetworkRepo.getById(memberId);
            assertThat(afterUpdate).isNotNull();
            assertThat(afterUpdate.memberId().value()).isEqualTo(memberId);
        }
    }
}
