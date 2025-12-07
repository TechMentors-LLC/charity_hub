package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Case.NewCaseProbs;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
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
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CaseRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class CaseRepoIntegrationTest {

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
    private ICaseRepo caseRepo;

    private int testCaseCode;

    @BeforeEach
    void setUp() {
        // Generate unique case code for each test
        testCaseCode = caseRepo.nextCaseCode();
    }

    @Nested
    @DisplayName("Case CRUD Operations")
    class CaseCrudOperations {

        @Test
        @DisplayName("Should save and retrieve case by code")
        void shouldSaveAndRetrieveCaseByCode() {
            Case case_ = createTestCase(testCaseCode);
            
            caseRepo.save(case_);
            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(testCaseCode));

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getCaseCode().value()).isEqualTo(testCaseCode);
        }

        @Test
        @DisplayName("Should return empty when case not found")
        void shouldReturnEmptyWhenCaseNotFound() {
            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(99999));

            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("Should update existing case")
        void shouldUpdateExistingCase() {
            Case case_ = createTestCase(testCaseCode);
            caseRepo.save(case_);

            // Update the case
            case_.update("Updated Title", "Updated Description", 2000, true, Collections.emptyList());
            caseRepo.save(case_);

            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(testCaseCode));
            assertThat(retrieved).isPresent();
        }

        @Test
        @DisplayName("Should delete draft case by code")
        void shouldDeleteDraftCaseByCode() {
            Case case_ = createTestCase(testCaseCode);
            caseRepo.save(case_);

            caseRepo.delete(new CaseCode(testCaseCode));

            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(testCaseCode));
            assertThat(retrieved).isEmpty();
        }
    }

    @Nested
    @DisplayName("Case with Contributions")
    class CaseWithContributions {

        @Test
        @DisplayName("Should save case with contributions")
        void shouldSaveCaseWithContributions() {
            Case case_ = createTestCase(testCaseCode);
            case_.open();
            
            UUID contributorId = UUID.randomUUID();
            case_.contribute(contributorId, 500);
            
            caseRepo.save(case_);

            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(testCaseCode));
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getContributions()).hasSize(1);
            assertThat(retrieved.get().getContributions().get(0).getContributorId()).isEqualTo(contributorId);
        }

        @Test
        @DisplayName("Should preserve contribution status after save")
        void shouldPreserveContributionStatusAfterSave() {
            Case case_ = createTestCase(testCaseCode);
            case_.open();
            
            UUID contributorId = UUID.randomUUID();
            Contribution contribution = case_.contribute(contributorId, 500);
            contribution.pay("proof-url");
            
            caseRepo.save(case_);

            Optional<Case> retrieved = caseRepo.getByCode(new CaseCode(testCaseCode));
            assertThat(retrieved).isPresent();
            Contribution retrievedContribution = retrieved.get().getContributions().get(0);
            assertThat(retrievedContribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
            assertThat(retrievedContribution.getPaymentProof()).isEqualTo("proof-url");
        }
    }

    @Nested
    @DisplayName("Case Code Generation")
    class CaseCodeGeneration {

        @Test
        @DisplayName("Should generate incrementing case codes")
        void shouldGenerateIncrementingCaseCodes() {
            int code1 = caseRepo.nextCaseCode();
            
            // Save a case with code1
            Case case1 = createTestCase(code1);
            caseRepo.save(case1);
            
            int code2 = caseRepo.nextCaseCode();

            assertThat(code2).isGreaterThan(code1);
        }
    }

    private Case createTestCase(int code) {
        NewCaseProbs props = new NewCaseProbs(
                code,
                "Test Case",
                "Test Description",
                1000,
                Status.DRAFT,
                false,
                Collections.emptyList()
        );
        return Case.newCase(props);
    }
}
