package com.charity_hub.cases.internal.api.integration;

import com.charity_hub.cases.internal.api.dtos.PayContributionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Contribution Status Flow Integration Tests")
class ContributionStatusFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay with proof then confirm")
    void shouldCompleteFullFlowWithProof() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        String proofUrl = "https://example.com/proof.jpg";
        PayContributionRequest payRequest = new PayContributionRequest(proofUrl);

        // When - Pay with proof
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isOk());

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay without proof then confirm")
    void shouldCompleteFullFlowWithoutProof() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();

        // When - Pay without proof
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isOk());

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should complete full flow: pay with empty body then confirm")
    void shouldCompleteFullFlowWithEmptyPayBody() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();

        // When - Pay with empty JSON body
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Then - Confirm
        mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle pay endpoint with multiple proof URL formats")
    void shouldHandleMultipleProofUrlFormats() throws Exception {
        // Test with valid URL
        UUID contributionId1 = UUID.randomUUID();
        PayContributionRequest request1 = new PayContributionRequest("https://example.com/proof.jpg");
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        // Test with null
        UUID contributionId2 = UUID.randomUUID();
        PayContributionRequest request2 = new PayContributionRequest(null);
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());

        // Test with empty string
        UUID contributionId3 = UUID.randomUUID();
        PayContributionRequest request3 = new PayContributionRequest("");
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk());
    }
}
