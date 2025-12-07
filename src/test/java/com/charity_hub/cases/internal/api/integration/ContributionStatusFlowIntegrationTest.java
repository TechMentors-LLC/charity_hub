package com.charity_hub.cases.internal.api.integration;

import com.charity_hub.cases.internal.api.dtos.PayContributionRequest;
import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Contribution Status Flow Integration Tests")
@SuppressWarnings("resource") // MongoDBContainer is managed by Testcontainers lifecycle
class ContributionStatusFlowIntegrationTest {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICaseRepo caseRepo;

    private UUID testContributorId;
    private int testCaseCode;

    @BeforeEach
    void setUp() {
        testContributorId = UUID.randomUUID();
        testCaseCode = 20040;
    }

    private Contribution createPledgedContribution() {
        Contribution contribution = Contribution.create(
                UUID.randomUUID(),
                testContributorId,
                testCaseCode,
                1000,
                ContributionStatus.PLEDGED,
                new Date(),
                null
        );
        caseRepo.save(contribution);
        return contribution;
    }

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay with proof then confirm")
    void shouldCompleteFullFlowWithProof() throws Exception {
        // Given - Create a real contribution in the database
        Contribution contribution = createPledgedContribution();
        UUID contributionId = contribution.getId().value();
        String proofUrl = "https://example.com/proof.jpg";
        PayContributionRequest payRequest = new PayContributionRequest(proofUrl);

        // When - Pay with proof
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isOk());

        // Verify contribution is now PAID
        Contribution paidContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(paidContribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(paidContribution.getPaymentProof()).isEqualTo(proofUrl);

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());

        // Verify contribution is now CONFIRMED
        Contribution confirmedContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(confirmedContribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay without proof then confirm")
    void shouldCompleteFullFlowWithoutProof() throws Exception {
        // Given - Create a real contribution in the database
        Contribution contribution = createPledgedContribution();
        UUID contributionId = contribution.getId().value();

        // When - Pay without proof (empty body)
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isOk());

        // Verify contribution is now PAID
        Contribution paidContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(paidContribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());

        // Verify contribution is now CONFIRMED
        Contribution confirmedContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(confirmedContribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay with empty body then confirm")
    void shouldCompleteFullFlowWithEmptyPayBody() throws Exception {
        // Given - Create a real contribution in the database
        Contribution contribution = createPledgedContribution();
        UUID contributionId = contribution.getId().value();

        // When - Pay with empty JSON body
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Verify contribution is now PAID
        Contribution paidContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(paidContribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());

        // Verify contribution is now CONFIRMED
        Contribution confirmedContribution = caseRepo.getContributionById(contributionId).orElseThrow();
        assertThat(confirmedContribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 404 when contribution does not exist")
    void shouldReturn404WhenContributionNotFound() throws Exception {
        // Given - A random UUID that doesn't exist
        UUID nonExistentContributionId = UUID.randomUUID();

        // When/Then - Pay should return 404
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", nonExistentContributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 409 when trying to pay already paid contribution")
    void shouldReturn409WhenPayingAlreadyPaidContribution() throws Exception {
        // Given - Create and pay a contribution
        Contribution contribution = createPledgedContribution();
        UUID contributionId = contribution.getId().value();

        // Pay it first
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isOk());

        // When/Then - Trying to pay again should return 409 Conflict
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 409 when trying to confirm unpaid contribution")
    void shouldReturn409WhenConfirmingUnpaidContribution() throws Exception {
        // Given - Create a pledged contribution (not paid)
        Contribution contribution = createPledgedContribution();
        UUID contributionId = contribution.getId().value();

        // When/Then - Trying to confirm without paying should return 409 Conflict
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isConflict());
    }
}
