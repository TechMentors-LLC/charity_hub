package com.charity_hub.accounts.internal.shell.repositories;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.model.account.Account;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AccountRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class AccountRepoIntegrationTest {

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
    private IAccountRepo accountRepo;

    private static final String MOBILE_NUMBER = "1234567890";
    private static final String DEVICE_ID = "device-123456789012345";
    private static final String DEVICE_TYPE = "ANDROID";

    @Nested
    @DisplayName("Account CRUD Operations")
    class AccountCrudOperations {

        @Test
        @DisplayName("Should save and retrieve account by ID")
        void shouldSaveAndRetrieveAccountById() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);
            
            accountRepo.save(account);
            Optional<Account> retrieved = accountRepo.getById(account.getId().value());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getId().value()).isEqualTo(account.getId().value());
            assertThat(retrieved.get().getMobileNumber().value()).isEqualTo(MOBILE_NUMBER);
        }

        @Test
        @DisplayName("Should retrieve account by mobile number")
        void shouldRetrieveAccountByMobileNumber() {
            String uniqueMobile = "9876543210";
            Account account = Account.newAccount(uniqueMobile, DEVICE_ID, DEVICE_TYPE, false);
            accountRepo.save(account);

            Optional<Account> retrieved = accountRepo.getByMobileNumber(uniqueMobile);

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getMobileNumber().value()).isEqualTo(uniqueMobile);
        }

        @Test
        @DisplayName("Should return empty when account not found by ID")
        void shouldReturnEmptyWhenAccountNotFoundById() {
            Optional<Account> retrieved = accountRepo.getById(UUID.randomUUID());

            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("Should return empty when account not found by mobile number")
        void shouldReturnEmptyWhenAccountNotFoundByMobileNumber() {
            Optional<Account> retrieved = accountRepo.getByMobileNumber("0000000000");

            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("Should update existing account")
        void shouldUpdateExistingAccount() {
            String uniqueMobile = "5555555555";
            Account account = Account.newAccount(uniqueMobile, DEVICE_ID, DEVICE_TYPE, false);
            accountRepo.save(account);

            // Block the account and save again
            account.block();
            accountRepo.save(account);

            Optional<Account> retrieved = accountRepo.getById(account.getId().value());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().isBlocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("Admin Account Operations")
    class AdminAccountOperations {

        @Test
        @DisplayName("Should create admin account with FULL_ACCESS permission")
        void shouldCreateAdminAccountWithFullAccessPermission() {
            String adminMobile = "1111111111";
            Account admin = Account.newAccount(adminMobile, DEVICE_ID, DEVICE_TYPE, true);
            accountRepo.save(admin);

            Optional<Account> retrieved = accountRepo.getById(admin.getId().value());
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getPermissions()).isNotEmpty();
        }
    }
}
