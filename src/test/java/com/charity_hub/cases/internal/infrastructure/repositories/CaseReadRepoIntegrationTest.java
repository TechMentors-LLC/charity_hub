package com.charity_hub.cases.internal.infrastructure.repositories;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.NewCaseProbs;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.mongodb.client.model.Filters;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CaseReadRepo Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class CaseReadRepoIntegrationTest {

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
    private ICaseReadRepo caseReadRepo;

    @Autowired
    private ICaseRepo caseRepo;

    private int testCaseCode1;
    private int testCaseCode2;
    private int testCaseCode3;

    @BeforeEach
    void setUp() {
        testCaseCode1 = caseRepo.nextCaseCode();
        testCaseCode2 = caseRepo.nextCaseCode();
        testCaseCode3 = caseRepo.nextCaseCode();
    }

    @Nested
    @DisplayName("getByCode Tests")
    class GetByCodeTests {

        @Test
        @DisplayName("Should return case entity when case exists")
        void shouldReturnCaseEntityWhenCaseExists() {
            createAndSaveCase(testCaseCode1, Status.OPENED);

            CaseEntity result = caseReadRepo.getByCode(testCaseCode1);

            assertThat(result).isNotNull();
            assertThat(result.code()).isEqualTo(testCaseCode1);
            assertThat(result.title()).isEqualTo("Test Case");
        }

        @Test
        @DisplayName("Should return null when case does not exist")
        void shouldReturnNullWhenCaseDoesNotExist() {
            CaseEntity result = caseReadRepo.getByCode(99999);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getByCodes Tests")
    class GetByCodesTests {

        @Test
        @DisplayName("Should return multiple cases by codes")
        void shouldReturnMultipleCasesByCodes() {
            // Get fresh case codes to ensure uniqueness
            int code1 = caseRepo.nextCaseCode();
            createAndSaveCase(code1, Status.OPENED);
            
            int code2 = caseRepo.nextCaseCode();
            createAndSaveCase(code2, Status.OPENED);

            List<CaseEntity> results = caseReadRepo.getByCodes(List.of(code1, code2));

            assertThat(results).hasSize(2);
            assertThat(results.stream().map(CaseEntity::code))
                    .containsExactlyInAnyOrder(code1, code2);
        }

        @Test
        @DisplayName("Should return empty list when no cases match")
        void shouldReturnEmptyListWhenNoCasesMatch() {
            List<CaseEntity> results = caseReadRepo.getByCodes(List.of(88888, 77777));

            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should return partial results when some codes match")
        void shouldReturnPartialResultsWhenSomeCodesMatch() {
            createAndSaveCase(testCaseCode1, Status.OPENED);

            List<CaseEntity> results = caseReadRepo.getByCodes(List.of(testCaseCode1, 99999));

            assertThat(results).hasSize(1);
            assertThat(results.get(0).code()).isEqualTo(testCaseCode1);
        }
    }

    @Nested
    @DisplayName("getContributionsByCaseCode Tests")
    class GetContributionsByCaseCodeTests {

        @Test
        @DisplayName("Should return contributions for a case")
        void shouldReturnContributionsForCase() {
            Case case_ = createAndSaveCase(testCaseCode1, Status.OPENED);
            UUID contributorId = UUID.randomUUID();
            case_.contribute(contributorId, 500);
            caseRepo.save(case_);

            List<ContributionEntity> results = caseReadRepo.getContributionsByCaseCode(testCaseCode1);

            assertThat(results).hasSize(1);
            assertThat(results.get(0).caseCode()).isEqualTo(testCaseCode1);
            assertThat(results.get(0).amount()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should return empty list when case has no contributions")
        void shouldReturnEmptyListWhenCaseHasNoContributions() {
            createAndSaveCase(testCaseCode1, Status.OPENED);

            List<ContributionEntity> results = caseReadRepo.getContributionsByCaseCode(testCaseCode1);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getDraftCases Tests")
    class GetDraftCasesTests {

        @Test
        @DisplayName("Should return only draft cases")
        void shouldReturnOnlyDraftCases() {
            createAndSaveCase(testCaseCode1, Status.DRAFT);
            createAndSaveCase(testCaseCode2, Status.OPENED);
            createAndSaveCase(testCaseCode3, Status.DRAFT);

            List<CaseEntity> results = caseReadRepo.getDraftCases();

            // Should only contain draft cases
            assertThat(results.stream().allMatch(c -> c.status() == CaseEntity.STATUS_DRAFT)).isTrue();
            assertThat(results.stream().map(CaseEntity::code))
                    .contains(testCaseCode1, testCaseCode3);
        }

        @Test
        @DisplayName("Should return empty list when no draft cases exist")
        void shouldReturnEmptyWhenNoDraftCases() {
            createAndSaveCase(testCaseCode1, Status.OPENED);

            List<CaseEntity> results = caseReadRepo.getDraftCases();

            // Filter to just our test case to avoid interference from other tests
            List<CaseEntity> ourCases = results.stream()
                    .filter(c -> c.code() == testCaseCode1)
                    .toList();
            assertThat(ourCases).isEmpty();
        }
    }

    @Nested
    @DisplayName("search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should return non-draft cases with pagination")
        void shouldReturnNonDraftCasesWithPagination() {
            createAndSaveCase(testCaseCode1, Status.OPENED);
            createAndSaveCase(testCaseCode2, Status.OPENED);
            createAndSaveCase(testCaseCode3, Status.DRAFT);

            List<CaseEntity> results = caseReadRepo.search(0, 10, null);

            // Should not include draft case
            assertThat(results.stream().noneMatch(c -> c.status() == CaseEntity.STATUS_DRAFT)).isTrue();
        }

        @Test
        @DisplayName("Should apply filter when provided")
        void shouldApplyFilterWhenProvided() {
            createAndSaveCase(testCaseCode1, Status.OPENED);
            createAndSaveCase(testCaseCode2, Status.OPENED);

            List<CaseEntity> results = caseReadRepo.search(0, 10, 
                () -> Filters.eq("code", testCaseCode1));

            assertThat(results).hasSize(1);
            assertThat(results.get(0).code()).isEqualTo(testCaseCode1);
        }

        @Test
        @DisplayName("Should respect offset and limit")
        void shouldRespectOffsetAndLimit() {
            createAndSaveCase(testCaseCode1, Status.OPENED);
            createAndSaveCase(testCaseCode2, Status.OPENED);
            createAndSaveCase(testCaseCode3, Status.OPENED);

            List<CaseEntity> firstPage = caseReadRepo.search(0, 2, null);
            List<CaseEntity> secondPage = caseReadRepo.search(2, 2, null);

            assertThat(firstPage.size()).isLessThanOrEqualTo(2);
            assertThat(secondPage).isNotNull();
        }
    }

    @Nested
    @DisplayName("getCasesCount Tests")
    class GetCasesCountTests {

        @Test
        @DisplayName("Should count non-draft cases")
        void shouldCountNonDraftCases() {
            // Note: This test adds to existing cases in DB
            int initialCount = caseReadRepo.getCasesCount(null);
            
            // Get fresh case codes and save immediately to ensure proper sequencing
            int code1 = caseRepo.nextCaseCode();
            createAndSaveCase(code1, Status.OPENED);
            
            int code2 = caseRepo.nextCaseCode();
            createAndSaveCase(code2, Status.DRAFT);
            
            int code3 = caseRepo.nextCaseCode();
            createAndSaveCase(code3, Status.OPENED);

            int newCount = caseReadRepo.getCasesCount(null);

            // Should have increased by 2 (only opened cases, not draft)
            assertThat(newCount).isEqualTo(initialCount + 2);
        }

        @Test
        @DisplayName("Should apply filter when counting")
        void shouldApplyFilterWhenCounting() {
            createAndSaveCase(testCaseCode1, Status.OPENED);
            createAndSaveCase(testCaseCode2, Status.OPENED);

            int count = caseReadRepo.getCasesCount(() -> Filters.eq("code", testCaseCode1));

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getContributions Tests")
    class GetContributionsTests {

        @Test
        @DisplayName("Should return contributions for single contributor")
        void shouldReturnContributionsForSingleContributor() {
            UUID contributorId = UUID.randomUUID();
            
            Case case_ = createAndSaveCase(testCaseCode1, Status.OPENED);
            case_.contribute(contributorId, 100);
            case_.contribute(contributorId, 200);
            caseRepo.save(case_);

            List<ContributionEntity> results = caseReadRepo.getContributions(contributorId);

            assertThat(results).hasSize(2);
            assertThat(results.stream().allMatch(c -> c.contributorId().equals(contributorId.toString()))).isTrue();
        }

        @Test
        @DisplayName("Should return contributions for multiple contributors")
        void shouldReturnContributionsForMultipleContributors() {
            UUID contributor1 = UUID.randomUUID();
            UUID contributor2 = UUID.randomUUID();
            
            Case case_ = createAndSaveCase(testCaseCode1, Status.OPENED);
            case_.contribute(contributor1, 100);
            case_.contribute(contributor2, 200);
            caseRepo.save(case_);

            List<ContributionEntity> results = caseReadRepo.getContributions(List.of(contributor1, contributor2));

            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when contributor has no contributions")
        void shouldReturnEmptyWhenNoContributions() {
            UUID contributorId = UUID.randomUUID();

            List<ContributionEntity> results = caseReadRepo.getContributions(contributorId);

            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getNotConfirmedContributions Tests")
    class GetNotConfirmedContributionsTests {

        @Test
        @DisplayName("Should return pledged and paid contributions but not confirmed")
        void shouldReturnNotConfirmedContributions() {
            UUID contributorId = UUID.randomUUID();
            
            Case case_ = createAndSaveCase(testCaseCode1, Status.OPENED);
            case_.contribute(contributorId, 100);
            Contribution paid = case_.contribute(contributorId, 200);
            paid.pay("proof-url");
            Contribution confirmed = case_.contribute(contributorId, 300);
            confirmed.pay("proof-url-2");
            confirmed.confirm();
            caseRepo.save(case_);

            List<ContributionEntity> results = caseReadRepo.getNotConfirmedContributions(contributorId);

            // Should return pledged and paid, but not confirmed
            assertThat(results).hasSize(2);
            assertThat(results.stream().noneMatch(c -> c.status() == ContributionEntity.STATUS_CONFIRMED)).isTrue();
        }

        @Test
        @DisplayName("Should return empty when all contributions are confirmed")
        void shouldReturnEmptyWhenAllConfirmed() {
            UUID contributorId = UUID.randomUUID();
            
            Case case_ = createAndSaveCase(testCaseCode1, Status.OPENED);
            Contribution contribution = case_.contribute(contributorId, 100);
            contribution.pay("proof");
            contribution.confirm();
            caseRepo.save(case_);

            List<ContributionEntity> results = caseReadRepo.getNotConfirmedContributions(contributorId);

            assertThat(results).isEmpty();
        }
    }

    private Case createAndSaveCase(int code, Status status) {
        NewCaseProbs props = new NewCaseProbs(
                code,
                "Test Case",
                "Test Description",
                1000,
                Status.DRAFT,
                false,
                Collections.emptyList()
        );
        Case case_ = Case.newCase(props);
        
        if (status == Status.OPENED) {
            case_.open();
        } else if (status == Status.CLOSED) {
            case_.open();
            case_.close();
        }
        
        caseRepo.save(case_);
        return case_;
    }
}
