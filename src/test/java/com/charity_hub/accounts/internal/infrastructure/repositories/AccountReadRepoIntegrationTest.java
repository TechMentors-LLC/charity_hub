package com.charity_hub.accounts.internal.infrastructure.repositories;

import com.charity_hub.accounts.internal.application.contracts.IAccountReadRepo;
import com.charity_hub.accounts.internal.application.queries.Account;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AccountReadRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class AccountReadRepoIntegrationTest {

    private static final MongoDBContainer mongoDBContainer;
    private static MongoClient directMongoClient;

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
    private IAccountReadRepo accountReadRepo;

    private MongoCollection<Document> accountsCollection;

    @BeforeEach
    void setUp() {
        // Create a direct MongoDB client for raw document operations
        if (directMongoClient == null) {
            directMongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());
        }
        MongoDatabase db = directMongoClient.getDatabase("test");
        accountsCollection = db.getCollection("accounts");
    }

    @Nested
    @DisplayName("getConnections Tests")
    class GetConnectionsTests {

        @Test
        @DisplayName("Should return accounts that have the user in their connections")
        void shouldReturnAccountsWithUserInConnections() {
            UUID targetUserId = UUID.randomUUID();
            UUID account1Id = UUID.randomUUID();
            UUID account2Id = UUID.randomUUID();
            
            // Create accounts with connections to the target user
            Document account1 = createAccountDocumentWithConnection(account1Id, "User One", targetUserId);
            Document account2 = createAccountDocumentWithConnection(account2Id, "User Two", targetUserId);
            
            accountsCollection.insertMany(List.of(account1, account2));

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).hasSize(2);
            assertThat(results.stream().map(Account::fullName))
                    .containsExactlyInAnyOrder("User One", "User Two");
        }

        @Test
        @DisplayName("Should return empty list when no accounts have user in connections")
        void shouldReturnEmptyWhenNoConnections() {
            UUID targetUserId = UUID.randomUUID();

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should not return accounts that don't have the user in connections")
        void shouldNotReturnAccountsWithoutUserInConnections() {
            UUID targetUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            
            // Create account connected to a different user
            Document account = createAccountDocumentWithConnection(accountId, "Other User", otherUserId);
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should correctly map account fields to query model")
        void shouldCorrectlyMapAccountFields() {
            UUID targetUserId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            
            Document account = new Document()
                    .append("accountId", accountId.toString())
                    .append("mobileNumber", "1234567890")
                    .append("fullName", "Test User")
                    .append("photoUrl", "https://example.com/photo.jpg")
                    .append("blocked", false)
                    .append("joinedDate", System.currentTimeMillis())
                    .append("lastUpdated", System.currentTimeMillis())
                    .append("permissions", List.of("READ_CASES", "WRITE_CASES"))
                    .append("devices", List.of())
                    .append("connections", List.of(
                            new Document("userId", targetUserId.toString())
                    ));
            
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).hasSize(1);
            Account result = results.get(0);
            assertThat(result.uuid()).isEqualTo(accountId.toString());
            assertThat(result.fullName()).isEqualTo("Test User");
            assertThat(result.photoUrl()).isEqualTo("https://example.com/photo.jpg");
            assertThat(result.permissions()).containsExactlyInAnyOrder("READ_CASES", "WRITE_CASES");
        }

        @Test
        @DisplayName("Should handle account with null fullName by returning Unknown")
        void shouldHandleNullFullName() {
            UUID targetUserId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            
            Document account = new Document()
                    .append("accountId", accountId.toString())
                    .append("mobileNumber", "1234567890")
                    .append("fullName", null)
                    .append("photoUrl", null)
                    .append("blocked", false)
                    .append("joinedDate", System.currentTimeMillis())
                    .append("lastUpdated", System.currentTimeMillis())
                    .append("permissions", List.of())
                    .append("devices", List.of())
                    .append("connections", List.of(
                            new Document("userId", targetUserId.toString())
                    ));
            
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).fullName()).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("Should handle account with multiple connections")
        void shouldHandleAccountWithMultipleConnections() {
            UUID targetUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            
            // Account connected to multiple users
            Document account = new Document()
                    .append("accountId", accountId.toString())
                    .append("mobileNumber", "1234567890")
                    .append("fullName", "Multi Connection User")
                    .append("photoUrl", null)
                    .append("blocked", false)
                    .append("joinedDate", System.currentTimeMillis())
                    .append("lastUpdated", System.currentTimeMillis())
                    .append("permissions", List.of())
                    .append("devices", List.of())
                    .append("connections", List.of(
                            new Document("userId", targetUserId.toString()),
                            new Document("userId", otherUserId.toString())
                    ));
            
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).fullName()).isEqualTo("Multi Connection User");
        }
    }

    private Document createAccountDocumentWithConnection(UUID accountId, String fullName, UUID connectionUserId) {
        return new Document()
                .append("accountId", accountId.toString())
                .append("mobileNumber", "1234567890")
                .append("fullName", fullName)
                .append("photoUrl", null)
                .append("blocked", false)
                .append("joinedDate", System.currentTimeMillis())
                .append("lastUpdated", System.currentTimeMillis())
                .append("permissions", List.of())
                .append("devices", List.of())
                .append("connections", List.of(
                        new Document("userId", connectionUserId.toString())
                ));
    }
}
