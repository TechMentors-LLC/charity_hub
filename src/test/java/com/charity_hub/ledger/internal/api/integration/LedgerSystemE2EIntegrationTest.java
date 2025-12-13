package com.charity_hub.ledger.internal.api.integration;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.eventHandlers.ContributionPaidHandler;
import com.charity_hub.ledger.internal.application.eventHandlers.ContributionConfirmedHandler;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * E2E Integration Tests for the complete ledger system lifecycle.
 * Tests the flow: Account Creation → Contribution (Pledged) → Payment →
 * Confirmation → Ledger Balance Updates
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ledger System E2E Integration Tests")
@SuppressWarnings("resource")
public class LedgerSystemE2EIntegrationTest {

        private static final MongoDBContainer mongoDBContainer;

        static {
                mongoDBContainer = new MongoDBContainer("mongo:7.0")
                                .withStartupTimeout(Duration.ofMinutes(2))
                                .withReuse(true);
                mongoDBContainer.start();
        }

        @DynamicPropertySource
        static void setProperties(DynamicPropertyRegistry registry) {
                String mongoUri = mongoDBContainer.getReplicaSetUrl() +
                                "?serverSelectionTimeoutMS=1000&connectTimeoutMS=1000&socketTimeoutMS=1000";
                registry.add("spring.data.mongodb.uri", () -> mongoUri);
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private IAccountRepo accountRepo;

        @Autowired
        private MembersNetworkRepo membersNetworkRepo;

        @Autowired
        private ILedgerRepository ledgerRepository;

        @Autowired
        private ICaseRepo caseRepo;

        @Autowired(required = false)
        private ContributionPaidHandler contributionPaidHandler;

        @Autowired(required = false)
        private ContributionConfirmedHandler contributionConfirmedHandler;

        @Autowired
        private com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContributionHandler confirmContributionHandlerCases;

        @Test
        @WithMockUser
        @DisplayName("Test Event Handlers Are Created")
        void shouldCheckEventHandlersExist() {
                assertThat(contributionPaidHandler).as("ContributionPaidHandler should be created").isNotNull();
                assertThat(contributionConfirmedHandler).as("ContributionConfirmedHandler should be created")
                                .isNotNull();
        }

        @Test
        @WithMockUser(authorities = "FULL_ACCESS")
        @DisplayName("Complete E2E Flow: Account → Contribution → Payment → Confirm → Verify Ledger")
        void shouldCompleteFullLedgerLifecycle() throws Exception {
                // ====================================================================
                // STEP 1: Create Root Account with Member and Ledger
                // ====================================================================
                Account rootAccount = Account.newAccount("1000000000", "device-root-12345", "ANDROID", true);
                accountRepo.save(rootAccount);
                UUID rootId = rootAccount.getId().value();

                // Create root member (no parent) - MOCKING the Network Repo behavior
                Member rootMember = new Member(
                                new MemberId(rootId),
                                null, // Root has no parent
                                Collections.emptyList(),
                                Collections.emptyList());
                membersNetworkRepo.save(rootMember);

                // Create root ledger
                Ledger rootLedger = Ledger.createNew(new MemberId(rootId));
                ledgerRepository.save(rootLedger);

                // Verify root ledger
                rootLedger = ledgerRepository.findByMemberId(new MemberId(rootId));
                assertThat(rootLedger.getDueAmount().value()).isEqualTo(0);
                assertThat(rootLedger.getDueNetworkAmount().value()).isEqualTo(0);

                // ====================================================================
                // STEP 2: Create Child Account with Parent Relationship
                // ====================================================================
                Account childAccount = Account.newAccount("2000000000", "device-child-1234", "ANDROID", false);
                accountRepo.save(childAccount);
                UUID childId = childAccount.getId().value();

                // Create child member with parent=root. Update root to have child.
                Member childMember = new Member(
                                new MemberId(childId),
                                new MemberId(rootId), // Parent is root
                                List.of(new MemberId(rootId)), // Ancestors include root
                                Collections.emptyList());
                membersNetworkRepo.save(childMember);

                // Update root to include child in children list
                rootMember = new Member(
                                new MemberId(rootId),
                                null,
                                Collections.emptyList(),
                                List.of(new MemberId(childId)));
                membersNetworkRepo.save(rootMember);

                // Create child ledger
                Ledger childLedger = Ledger.createNew(new MemberId(childId));
                ledgerRepository.save(childLedger);

                System.out.println("✓ STEP 1 & 2: Accounts and Ledgers Setup Completed");

                // ====================================================================
                // STEP 3: Create Contribution (Pledge)
                // This triggers ContributionMade -> Updates Ledger
                // ====================================================================
                int contributionAmount = 1000;
                // Contribution.create RAISES ContributionMade event (impl detail)
                Contribution contribution = Contribution.new_(
                                childId,
                                99999, // Test case code
                                contributionAmount);
                UUID contributionId = contribution.getId().value();
                // Saving should dispatch the event to handlers
                caseRepo.save(contribution);

                Thread.sleep(2000); // Wait for async event processing

                // Verify Child Ledger: Due should increase (Pledged)
                childLedger = ledgerRepository.findByMemberId(new MemberId(childId));
                assertThat(childLedger.getDueAmount().value()).isEqualTo(contributionAmount);

                // Verify Root Ledger: DueNetwork should increase (Expectation from child)
                rootLedger = ledgerRepository.findByMemberId(new MemberId(rootId));
                assertThat(rootLedger.getDueNetworkAmount().value()).isEqualTo(contributionAmount);
                // Root Due should remain 0 (it's their child's debt, not theirs yet)
                assertThat(rootLedger.getDueAmount().value()).isEqualTo(0);

                System.out.println("✓ STEP 3: Pledge Verified on Ledgers");

                // ====================================================================
                // STEP 4: Pay Contribution
                // This triggers ContributionPaid -> Ledger UNCHANGED (stateless pay)
                // ====================================================================
                mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                                .andExpect(status().isOk());

                Thread.sleep(2000);

                // Check Ledger Unchanged
                childLedger = ledgerRepository.findByMemberId(new MemberId(childId));
                assertThat(childLedger.getDueAmount().value()).isEqualTo(contributionAmount); // Still "Due" until
                                                                                              // confirmed

                // Check Contribution Status in DB (should be PAID)
                Contribution paidContribution = caseRepo.getContributionById(contributionId).get();
                assertThat(paidContribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);

                System.out.println("✓ STEP 4: Payment Verified (Ledger Unchanged, Status Updated)");

                // ====================================================================
                // STEP 5: Confirm Contribution
                // This triggers ContributionConfirmed -> Updates Ledger
                // ====================================================================
                // Calling handler directly to simulate Authorized PARENT action

                com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution confirmCmd = new com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution(
                                contributionId, rootId);

                confirmContributionHandlerCases.handle(confirmCmd);

                Thread.sleep(2000); // Wait for async Ledger update

                // Verify Balances:
                // 1. Child Due should be CLEARED (was 1000, now 0) - because debt is confirmed
                // passed up
                childLedger = ledgerRepository.findByMemberId(new MemberId(childId));
                assertThat(childLedger.getDueAmount().value()).isEqualTo(0);

                // 2. Child DueNetwork? Should be 0 (no network).
                assertThat(childLedger.getDueNetworkAmount().value()).isEqualTo(0);

                // 3. Root DueNetwork should be CLEARED (was 1000, expecting from child -> now
                // confirmed)
                rootLedger = ledgerRepository.findByMemberId(new MemberId(rootId));
                assertThat(rootLedger.getDueNetworkAmount().value()).isEqualTo(0);

                // 4. Root Due should INCREASE (was 0 -> now 1000) - Parent now owns the
                // obligation up
                assertThat(rootLedger.getDueAmount().value()).isEqualTo(contributionAmount);

                System.out.println("✓ STEP 5: Confirmation Verified (Ledgers Settled and Cascaded)");

                // ====================================================================
                // STEP 6: Test Query Endpoints
                // ====================================================================
                mockMvc.perform(get("/v1/ledger/{userId}", childId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.contributions").isArray());

                // Test Summary Endpoint with manual authentication (Parent seeing connections)
                com.charity_hub.shared.auth.AccessTokenPayload payload = new com.charity_hub.shared.auth.AccessTokenPayload(
                                "test-aud", "test-jti", new Date(), new Date(),
                                rootId.toString(), "Test Parent", null, false, "1234567890", "test-device",
                                List.of("FULL_ACCESS"));
                org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                payload, "N/A",
                                java.util.List.of(
                                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                                "FULL_ACCESS")));

                mockMvc.perform(get("/v1/ledger/summary")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .authentication(auth)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.connectionsLedger", org.hamcrest.Matchers.hasSize(1)));

                System.out.println("✓ STEP 6: Query endpoints working\n");

                System.out.println("========================================");
                System.out.println("✅ E2E LEDGER TEST PASSED");
                System.out.println("========================================");
        }
}
