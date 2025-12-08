package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.ContributeRequest;
import com.charity_hub.cases.internal.application.commands.Contribute.Contribute;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeDefaultResponse;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContributionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ContributionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContributeHandler contributeHandler;

    @MockBean
    private BusinessMetrics businessMetrics;

    @Nested
    @DisplayName("POST /v1/cases/{caseCode}/contributions")
    class ContributeEndpoint {

        @Test
        @DisplayName("should create contribution successfully and return 200")
        void shouldCreateContributionSuccessfully() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var request = new ContributeRequest(5000);
            var expectedResponse = new ContributeDefaultResponse("contrib-123");
            when(contributeHandler.handle(any(Contribute.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post("/v1/cases/12345/contributions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributionId").value("contrib-123"));

            verify(contributeHandler).handle(any(Contribute.class));
        }

        @Test
        @DisplayName("should pass correct parameters to handler")
        void shouldPassCorrectParametersToHandler() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var request = new ContributeRequest(10000);
            when(contributeHandler.handle(any(Contribute.class))).thenReturn(new ContributeDefaultResponse("id"));

            // Act
            mockMvc.perform(post("/v1/cases/98765/contributions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<Contribute> captor = ArgumentCaptor.forClass(Contribute.class);
            verify(contributeHandler).handle(captor.capture());
            Contribute captured = captor.getValue();

            assertThat(captured.amount()).isEqualTo(10000);
            assertThat(captured.caseCode()).isEqualTo(98765);
            assertThat(captured.userId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("should handle small contribution amounts")
        void shouldHandleSmallContributionAmounts() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var request = new ContributeRequest(1);
            when(contributeHandler.handle(any(Contribute.class))).thenReturn(new ContributeDefaultResponse("small-contrib"));

            // Act & Assert
            mockMvc.perform(post("/v1/cases/1/contributions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributionId").value("small-contrib"));
        }

        @Test
        @DisplayName("should handle large contribution amounts")
        void shouldHandleLargeContributionAmounts() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var request = new ContributeRequest(1000000);
            when(contributeHandler.handle(any(Contribute.class))).thenReturn(new ContributeDefaultResponse("large-contrib"));

            // Act & Assert
            mockMvc.perform(post("/v1/cases/99999/contributions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributionId").value("large-contrib"));
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
                    List.of("CONTRIBUTE")
            );
        }

        private void setSecurityContext(AccessTokenPayload accessTokenPayload) {
            var authorities = accessTokenPayload.getPermissions().stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            var authentication = new UsernamePasswordAuthenticationToken(
                    accessTokenPayload,
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
