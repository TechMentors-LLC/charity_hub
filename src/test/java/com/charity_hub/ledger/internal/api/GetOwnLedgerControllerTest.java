package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.Contribution;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.ledger.internal.application.queries.GetLedger.LedgerResponse;
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

@WebMvcTest(GetOwnLedgerController.class)
@Import({ GlobalExceptionHandler.class, TestObservabilityConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class GetOwnLedgerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private GetLedgerHandler getLedgerHandler;

        @Nested
        @DisplayName("GET /v1/ledger/me")
        class GetOwnLedgerEndpoint {

                @Test
                @WithMockUser
                @DisplayName("should get own ledger successfully and return 200")
                void shouldGetOwnLedgerSuccessfully() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        var accessTokenPayload = createAccessTokenPayload(userId);
                        setSecurityContext(accessTokenPayload);

                        var contribution = new Contribution(
                                        "contrib-123",
                                        userId.toString(),
                                        12345,
                                        "Test Case",
                                        5000,
                                        "PAID",
                                        System.currentTimeMillis());
                        var expectedResponse = new LedgerResponse(List.of(contribution), 0, 0);
                        when(getLedgerHandler.handle(any(GetLedger.class))).thenReturn(expectedResponse);

                        // Act & Assert
                        mockMvc.perform(get("/v1/ledger/me"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.contributions").isArray())
                                        .andExpect(jsonPath("$.contributions[0].id").value("contrib-123"))
                                        .andExpect(jsonPath("$.contributions[0].amount").value(5000));

                        verify(getLedgerHandler).handle(any(GetLedger.class));
                }

                @Test
                @WithMockUser
                @DisplayName("should use authenticated user's ID from token")
                void shouldUseAuthenticatedUserId() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        var accessTokenPayload = createAccessTokenPayload(userId);
                        setSecurityContext(accessTokenPayload);

                        when(getLedgerHandler.handle(any(GetLedger.class)))
                                        .thenReturn(new LedgerResponse(List.of(), 0, 0));

                        // Act
                        mockMvc.perform(get("/v1/ledger/me"))
                                        .andExpect(status().isOk());

                        // Assert
                        ArgumentCaptor<GetLedger> captor = ArgumentCaptor.forClass(GetLedger.class);
                        verify(getLedgerHandler).handle(captor.capture());
                        GetLedger captured = captor.getValue();

                        assertThat(captured.userId()).isEqualTo(userId);
                }

                @Test
                @WithMockUser
                @DisplayName("should return empty list when user has no contributions")
                void shouldReturnEmptyListWhenNoContributions() throws Exception {
                        // Arrange
                        UUID userId = UUID.randomUUID();
                        var accessTokenPayload = createAccessTokenPayload(userId);
                        setSecurityContext(accessTokenPayload);

                        when(getLedgerHandler.handle(any(GetLedger.class)))
                                        .thenReturn(new LedgerResponse(List.of(), 0, 0));

                        // Act & Assert
                        mockMvc.perform(get("/v1/ledger/me"))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.contributions").isArray())
                                        .andExpect(jsonPath("$.contributions").isEmpty());
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
                                        List.of("CONTRIBUTE"));
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
}
