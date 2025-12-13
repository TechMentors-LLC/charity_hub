package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.PayContributionRequest;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContribution;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContributionHandler;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayContributionController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Pay Contribution Controller Tests")
class PayContributionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayContributionHandler handler;

    @Test
    @WithMockUser
    @DisplayName("Should pay contribution with proof URL when provided")
    void shouldPayContributionWithProofUrl() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        String proofUrl = "https://example.com/proof.jpg";
        PayContributionRequest request = new PayContributionRequest(proofUrl);
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify handler was called with correct command
        verify(handler).handle(argThat(command ->
                command.contributionId().equals(contributionId) &&
                command.paymentProof().equals(proofUrl)
        ));
    }

    @Test
    @WithMockUser
    @DisplayName("Should pay contribution without proof URL when not provided")
    void shouldPayContributionWithoutProofUrl() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Verify handler was called with null paymentProof
        verify(handler).handle(argThat(command ->
                command.contributionId().equals(contributionId) &&
                command.paymentProof() == null
        ));
    }

    @Test
    @WithMockUser
    @DisplayName("Should pay contribution when request body is empty")
    void shouldPayContributionWithEmptyBody() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isOk());

        // Verify handler was called with null paymentProof
        verify(handler).handle(argThat(command ->
                command.contributionId().equals(contributionId) &&
                command.paymentProof() == null
        ));
    }

    @Test
    @WithMockUser
    @DisplayName("Should pay contribution with null proof URL in request body")
    void shouldPayContributionWithNullProofUrlInBody() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        PayContributionRequest request = new PayContributionRequest(null);
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify handler was called with null paymentProof
        verify(handler).handle(argThat(command ->
                command.contributionId().equals(contributionId) &&
                command.paymentProof() == null
        ));
    }

    @Test
    @DisplayName("Should return 400 when contribution ID is invalid")
    void shouldReturn400WhenContributionIdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", "invalid-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should accept empty string proof URL")
    void shouldAcceptEmptyStringProofUrl() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        PayContributionRequest request = new PayContributionRequest("");
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When & Then
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify handler was called with empty string paymentProof
        verify(handler).handle(argThat(command ->
                command.contributionId().equals(contributionId) &&
                command.paymentProof().isEmpty()
        ));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle contribution ID correctly")
    void shouldHandleContributionIdCorrectly() throws Exception {
        // Given
        UUID contributionId = UUID.randomUUID();
        
        doNothing().when(handler).handle(any(PayContribution.class));

        // When
        mockMvc.perform(post("/v1/contributions/{contributionId}/pay", contributionId))
                .andExpect(status().isOk());

        // Then - Verify contribution ID is passed correctly
        verify(handler).handle(argThat(command -> 
                command.contributionId().equals(contributionId)
        ));
    }
}
