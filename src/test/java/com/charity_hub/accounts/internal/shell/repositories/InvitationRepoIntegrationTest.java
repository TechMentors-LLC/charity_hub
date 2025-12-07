package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("InvitationRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class InvitationRepoIntegrationTest {

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
    private IInvitationRepo invitationRepo;

    @Nested
    @DisplayName("Invitation CRUD Operations")
    class InvitationCrudOperations {

        @Test
        @DisplayName("Should save and retrieve invitation by mobile number")
        void shouldSaveAndRetrieveInvitationByMobileNumber() {
            // Use timestamp to ensure unique mobile number
            String mobileNumber = "22" + System.currentTimeMillis() % 100000000;
            UUID inviterId = UUID.randomUUID();
            Invitation invitation = Invitation.of(mobileNumber, inviterId);

            invitationRepo.save(invitation);
            Invitation retrieved = invitationRepo.get(mobileNumber);

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.invitedMobileNumber().value()).isEqualTo(mobileNumber);
            assertThat(retrieved.inviterId()).isEqualTo(inviterId);
        }

        @Test
        @DisplayName("Should return null when invitation not found")
        void shouldReturnNullWhenInvitationNotFound() {
            Invitation retrieved = invitationRepo.get("0000000000");

            assertThat(retrieved).isNull();
        }

        @Test
        @DisplayName("Should check if invitation exists")
        void shouldCheckIfInvitationExists() {
            String mobileNumber = "33" + System.currentTimeMillis() % 100000000;
            UUID inviterId = UUID.randomUUID();
            Invitation invitation = Invitation.of(mobileNumber, inviterId);

            invitationRepo.save(invitation);

            assertThat(invitationRepo.hasInvitation(mobileNumber)).isTrue();
            assertThat(invitationRepo.hasInvitation("9999999999")).isFalse();
        }

        @Test
        @DisplayName("Should handle saving invitation with different inviter for same mobile")
        void shouldHandleSavingInvitationWithDifferentInviterForSameMobile() {
            String mobileNumber = "44" + System.currentTimeMillis() % 100000000;
            UUID inviterId1 = UUID.randomUUID();
            UUID inviterId2 = UUID.randomUUID();
            
            // Save first invitation
            Invitation invitation1 = Invitation.of(mobileNumber, inviterId1);
            invitationRepo.save(invitation1);
            
            // Save second invitation with same mobile but different inviter
            // Note: The repo uses inviterId as the key, so this creates a new record
            Invitation invitation2 = Invitation.of(mobileNumber, inviterId2);
            invitationRepo.save(invitation2);

            // Should still be able to find by mobile number
            Invitation retrieved = invitationRepo.get(mobileNumber);
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.invitedMobileNumber().value()).isEqualTo(mobileNumber);
        }
    }
}
