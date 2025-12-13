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
        String mongoUri = mongoDBContainer.getReplicaSetUrl()
                + "?serverSelectionTimeoutMS=1000&connectTimeoutMS=1000&socketTimeoutMS=1000";
        registry.add("spring.data.mongodb.uri", () -> mongoUri);
    }

    @Autowired
    private IAccountReadRepo accountReadRepo;

    private MongoCollection<Document> accountsCollection;
    private MongoCollection<Document> connectionsCollection;

    @BeforeEach
    void setUp() {
        // Create a direct MongoDB client for raw document operations
        if (directMongoClient == null) {
            directMongoClient = MongoClients.create(mongoDBContainer.getReplicaSetUrl());
        }
        MongoDatabase db = directMongoClient.getDatabase("test");
        accountsCollection = db.getCollection("accounts");
        connectionsCollection = db.getCollection("connections");
        // Clean up before each test to ensure test isolation
        accountsCollection.deleteMany(new Document());
        connectionsCollection.deleteMany(new Document());
    }

    @Nested
    @DisplayName("getConnections Tests")
    class GetConnectionsTests {

        @Test
        @DisplayName("Should return accounts that are children of the user")
        void shouldReturnAccountsWithUserInConnections() {
            UUID parentUserId = UUID.randomUUID();
            UUID child1Id = UUID.randomUUID();
            UUID child2Id = UUID.randomUUID();

            // Create member entry in connections collection with children
            Document memberWithChildren = new Document()
                    .append("_id", parentUserId.toString())
                    .append("children", List.of(child1Id.toString(), child2Id.toString()));
            connectionsCollection.insertOne(memberWithChildren);

            // Create account documents for the children
            Document account1 = createAccountDocument(child1Id, "User One");
            Document account2 = createAccountDocument(child2Id, "User Two");
            accountsCollection.insertMany(List.of(account1, account2));

            List<Account> results = accountReadRepo.getConnections(parentUserId);

            assertThat(results).hasSize(2);
            assertThat(results.stream().map(Account::fullName))
                    .containsExactlyInAnyOrder("User One", "User Two");
        }

        @Test
        @DisplayName("Should return empty list when no member found in connections collection")
        void shouldReturnEmptyWhenNoMemberFound() {
            UUID targetUserId = UUID.randomUUID();

            List<Account> results = accountReadRepo.getConnections(targetUserId);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when member has no children")
        void shouldReturnEmptyWhenNoChildren() {
            UUID parentUserId = UUID.randomUUID();

            // Create member with empty children list
            Document memberWithoutChildren = new Document()
                    .append("_id", parentUserId.toString())
                    .append("children", List.of());
            connectionsCollection.insertOne(memberWithoutChildren);

            List<Account> results = accountReadRepo.getConnections(parentUserId);

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should correctly map account fields to query model")
        void shouldCorrectlyMapAccountFields() {
            UUID parentUserId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();

            // Create connection with child
            Document memberWithChild = new Document()
                    .append("_id", parentUserId.toString())
                    .append("children", List.of(childId.toString()));
            connectionsCollection.insertOne(memberWithChild);

            // Create account with all fields
            Document account = new Document()
                    .append("accountId", childId.toString())
                    .append("mobileNumber", "1234567890")
                    .append("fullName", "Test User")
                    .append("photoUrl", "https://example.com/photo.jpg")
                    .append("blocked", false)
                    .append("joinedDate", System.currentTimeMillis())
                    .append("lastUpdated", System.currentTimeMillis())
                    .append("permissions", List.of("READ_CASES", "WRITE_CASES"))
                    .append("devices", List.of());
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(parentUserId);

            assertThat(results).hasSize(1);
            Account result = results.get(0);
            assertThat(result.uuid()).isEqualTo(childId.toString());
            assertThat(result.fullName()).isEqualTo("Test User");
            assertThat(result.photoUrl()).isEqualTo("https://example.com/photo.jpg");
            assertThat(result.permissions()).containsExactlyInAnyOrder("READ_CASES", "WRITE_CASES");
        }

        @Test
        @DisplayName("Should handle account with null fullName by returning default value")
        void shouldHandleNullFullName() {
            UUID parentUserId = UUID.randomUUID();
            UUID childId = UUID.randomUUID();

            // Create connection with child
            Document memberWithChild = new Document()
                    .append("_id", parentUserId.toString())
                    .append("children", List.of(childId.toString()));
            connectionsCollection.insertOne(memberWithChild);

            // Create account with null fullName
            Document account = new Document()
                    .append("accountId", childId.toString())
                    .append("mobileNumber", "1234567890")
                    .append("fullName", null)
                    .append("photoUrl", null)
                    .append("blocked", false)
                    .append("joinedDate", System.currentTimeMillis())
                    .append("lastUpdated", System.currentTimeMillis())
                    .append("permissions", List.of())
                    .append("devices", List.of());
            accountsCollection.insertOne(account);

            List<Account> results = accountReadRepo.getConnections(parentUserId);

            assertThat(results).hasSize(1);
            // The implementation returns "Unknown" for null fullName via AccountReadMapper
            assertThat(results.get(0).fullName()).isNotNull();
        }

        @Test
        @DisplayName("Should handle member with multiple children")
        void shouldHandleAccountWithMultipleConnections() {
            UUID parentUserId = UUID.randomUUID();
            UUID child1Id = UUID.randomUUID();
            UUID child2Id = UUID.randomUUID();
            UUID child3Id = UUID.randomUUID();

            // Create member with multiple children
            Document memberWithChildren = new Document()
                    .append("_id", parentUserId.toString())
                    .append("children", List.of(
                            child1Id.toString(),
                            child2Id.toString(),
                            child3Id.toString()));
            connectionsCollection.insertOne(memberWithChildren);

            // Create account documents for the children
            accountsCollection.insertMany(List.of(
                    createAccountDocument(child1Id, "Child One"),
                    createAccountDocument(child2Id, "Child Two"),
                    createAccountDocument(child3Id, "Child Three")));

            List<Account> results = accountReadRepo.getConnections(parentUserId);

            assertThat(results).hasSize(3);
            assertThat(results.stream().map(Account::fullName))
                    .containsExactlyInAnyOrder("Child One", "Child Two", "Child Three");
        }
    }

    private Document createAccountDocument(UUID accountId, String fullName) {
        return new Document()
                .append("accountId", accountId.toString())
                .append("mobileNumber", "1234567890")
                .append("fullName", fullName)
                .append("photoUrl", null)
                .append("blocked", false)
                .append("joinedDate", System.currentTimeMillis())
                .append("lastUpdated", System.currentTimeMillis())
                .append("permissions", List.of())
                .append("devices", List.of());
    }
}
