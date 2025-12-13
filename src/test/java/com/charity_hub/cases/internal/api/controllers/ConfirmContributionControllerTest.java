package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution;
import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContributionHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfirmContributionController.class)
@Import({ GlobalExceptionHandler.class, TestObservabilityConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Confirm Contribution Controller Tests")
class ConfirmContributionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ConfirmContributionHandler handler;

        @Test
        @DisplayName("Should confirm contribution successfully")
        void shouldConfirmContribution() throws Exception {
                // Given
                UUID userId = UUID.randomUUID();
                UUID contributionId = UUID.randomUUID();
                setSecurityContext(createAccessTokenPayload(userId));

                doNothing().when(handler).handle(any(ConfirmContribution.class));

                // When & Then
                mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                                .andExpect(status().isOk());

                // Verify handler was called with correct command
                verify(handler).handle(argThat(command -> command.contributionId().equals(contributionId)));
        }

        @Test
        @DisplayName("Should confirm contribution without requiring request body")
        void shouldConfirmContributionWithoutBody() throws Exception {
                // Given
                UUID userId = UUID.randomUUID();
                UUID contributionId = UUID.randomUUID();
                setSecurityContext(createAccessTokenPayload(userId));

                doNothing().when(handler).handle(any(ConfirmContribution.class));

                // When & Then
                mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                                .andExpect(status().isOk());

                // Verify no request body is needed
                verify(handler).handle(argThat(command -> command.contributionId().equals(contributionId)));
        }

        @Test
        @DisplayName("Should return 400 when contribution ID is invalid")
        void shouldReturn400WhenContributionIdIsInvalid() throws Exception {
                // When & Then
                mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", "invalid-uuid"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle contribution ID correctly")
        void shouldHandleContributionIdCorrectly() throws Exception {
                // Given
                UUID userId = UUID.randomUUID();
                UUID contributionId = UUID.randomUUID();
                setSecurityContext(createAccessTokenPayload(userId));

                doNothing().when(handler).handle(any(ConfirmContribution.class));

                // When
                mockMvc.perform(post("/v1/contributions/{contributionId}/confirm", contributionId))
                                .andExpect(status().isOk());

                // Then - Verify contribution ID is passed correctly
                verify(handler).handle(argThat(command -> command.contributionId().equals(contributionId)));
        }

        private AccessTokenPayload createAccessTokenPayload(UUID userId) {
                return new AccessTokenPayload(
                                "test-audience",
                                "test-jwt-id",
                                new Date(System.currentTimeMillis() + 3600000),
                                new Date(),
                                userId.toString(),
                                "Test User",
                                "http://photo.url",
                                false,
                                "+1234567890",
                                "test-device-id",
                                List.of("CONFIRM_CONTRIBUTION"));
        }

        private void setSecurityContext(AccessTokenPayload accessTokenPayload) {
                var authorities = accessTokenPayload.getPermissions().stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();
                var authentication = new UsernamePasswordAuthenticationToken(
                                accessTokenPayload,
                                null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }
}
