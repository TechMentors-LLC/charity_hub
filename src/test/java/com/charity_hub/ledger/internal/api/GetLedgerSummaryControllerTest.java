package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedgerSummary.GetLedgerSummary;
import com.charity_hub.ledger.internal.application.queries.GetLedgerSummary.GetLedgerSummaryHandler;
import com.charity_hub.ledger.internal.application.queries.GetLedgerSummary.LedgerSummaryDefaultResponse;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetLedgerSummaryController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class GetLedgerSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetLedgerSummaryHandler getLedgerSummaryHandler;

    @Nested
    @DisplayName("GET /v1/ledger/summary")
    class GetLedgerSummaryEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should get ledger summary successfully and return 200")
        void shouldGetLedgerSummarySuccessfully() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var connectionLedger = new LedgerSummaryDefaultResponse.ConnectionLedger(
                    UUID.randomUUID().toString(),
                    "John Doe",
                    "http://photo.url",
                    1000,
                    500,
                    200
            );
            var expectedResponse = new LedgerSummaryDefaultResponse(
                    5000,
                    3000,
                    2000,
                    List.of(connectionLedger)
            );
            when(getLedgerSummaryHandler.handle(any(GetLedgerSummary.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.confirmed").value(5000))
                    .andExpect(jsonPath("$.pledged").value(3000))
                    .andExpect(jsonPath("$.paid").value(2000))
                    .andExpect(jsonPath("$.connectionsLedger").isArray())
                    .andExpect(jsonPath("$.connectionsLedger[0].name").value("John Doe"));

            verify(getLedgerSummaryHandler).handle(any(GetLedgerSummary.class));
        }

        @Test
        @WithMockUser
        @DisplayName("should use authenticated user's ID from token")
        void shouldUseAuthenticatedUserId() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            when(getLedgerSummaryHandler.handle(any(GetLedgerSummary.class)))
                    .thenReturn(new LedgerSummaryDefaultResponse(0, 0, 0, List.of()));

            // Act
            mockMvc.perform(get("/v1/ledger/summary"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetLedgerSummary> captor = ArgumentCaptor.forClass(GetLedgerSummary.class);
            verify(getLedgerSummaryHandler).handle(captor.capture());
            GetLedgerSummary captured = captor.getValue();

            assertThat(captured.userId()).isEqualTo(userId);
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty connections list when no connections")
        void shouldReturnEmptyConnectionsListWhenNoConnections() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            when(getLedgerSummaryHandler.handle(any(GetLedgerSummary.class)))
                    .thenReturn(new LedgerSummaryDefaultResponse(0, 0, 0, List.of()));

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.connectionsLedger").isArray())
                    .andExpect(jsonPath("$.connectionsLedger").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("should return summary with multiple connections")
        void shouldReturnSummaryWithMultipleConnections() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var connection1 = new LedgerSummaryDefaultResponse.ConnectionLedger(
                    UUID.randomUUID().toString(), "User 1", "http://photo1.url", 100, 50, 25);
            var connection2 = new LedgerSummaryDefaultResponse.ConnectionLedger(
                    UUID.randomUUID().toString(), "User 2", "http://photo2.url", 200, 100, 50);
            var expectedResponse = new LedgerSummaryDefaultResponse(
                    1000, 500, 300, List.of(connection1, connection2)
            );
            when(getLedgerSummaryHandler.handle(any(GetLedgerSummary.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.connectionsLedger[0].name").value("User 1"))
                    .andExpect(jsonPath("$.connectionsLedger[1].name").value("User 2"));
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
