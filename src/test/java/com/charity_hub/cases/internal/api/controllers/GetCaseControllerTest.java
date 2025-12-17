package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseQuery;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseResponse;
import com.charity_hub.cases.internal.application.queries.GetCase.IGetCaseHandler;
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

@WebMvcTest(GetCaseController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class GetCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IGetCaseHandler getCaseHandler;

    @Nested
    @DisplayName("GET /v1/cases/{caseCode}")
    class GetCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should get case successfully and return 200")
        void shouldGetCaseSuccessfully() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var caseDetails = new GetCaseResponse.CaseDetails(
                    12345,
                    "Test Case",
                    "Test Description",
                    10000,
                    5000,
                    true,
                    "OPEN",
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    List.of(),
                    List.of("doc1.pdf")
            );
            var expectedResponse = new GetCaseResponse(caseDetails);
            when(getCaseHandler.handle(any(GetCaseQuery.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/cases/12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.case.code").value(12345))
                    .andExpect(jsonPath("$.case.title").value("Test Case"))
                    .andExpect(jsonPath("$.case.goal").value(10000))
                    .andExpect(jsonPath("$.case.collected").value(5000))
                    .andExpect(jsonPath("$.case.status").value("OPEN"));

            verify(getCaseHandler).handle(any(GetCaseQuery.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass correct case code to handler")
        void shouldPassCorrectCaseCodeToHandler() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var caseDetails = new GetCaseResponse.CaseDetails(
                    54321, "Case", "Desc", 5000, 0, false, "DRAFT",
                    System.currentTimeMillis(), System.currentTimeMillis(), List.of(), List.of()
            );
            when(getCaseHandler.handle(any(GetCaseQuery.class))).thenReturn(new GetCaseResponse(caseDetails));

            // Act
            mockMvc.perform(get("/v1/cases/54321"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetCaseQuery> captor = ArgumentCaptor.forClass(GetCaseQuery.class);
            verify(getCaseHandler).handle(captor.capture());
            GetCaseQuery captured = captor.getValue();

            assertThat(captured.caseCode()).isEqualTo(54321);
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should return case with contributions")
        void shouldReturnCaseWithContributions() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId);
            setSecurityContext(accessTokenPayload);

            var contributor = new GetCaseResponse.Contributor("user-1", "John Doe", "http://photo.url");
            var contribution = new GetCaseResponse.Contribution(1000, contributor);
            var caseDetails = new GetCaseResponse.CaseDetails(
                    12345, "Charity Case", "Help needed", 20000, 15000, true, "OPEN",
                    System.currentTimeMillis(), System.currentTimeMillis(),
                    List.of(contribution), List.of()
            );
            when(getCaseHandler.handle(any(GetCaseQuery.class))).thenReturn(new GetCaseResponse(caseDetails));

            // Act & Assert
            mockMvc.perform(get("/v1/cases/12345"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.case.contributions").isArray())
                    .andExpect(jsonPath("$.case.contributions[0].amount").value(1000))
                    .andExpect(jsonPath("$.case.contributions[0].contributor.fullName").value("John Doe"));
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
                    List.of("FULL_ACCESS")
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
