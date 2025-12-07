package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.Contribution;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.ledger.internal.application.queries.GetLedger.LedgerResponse;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetLedgerController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GetLedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetLedgerHandler getLedgerHandler;

    @Nested
    @DisplayName("GET /v1/ledger/{userId}")
    class GetLedgerEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should get ledger successfully and return 200")
        void shouldGetLedgerSuccessfully() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var contribution = new Contribution(
                    "contrib-123",
                    userId.toString(),
                    12345,
                    "Test Case",
                    5000,
                    "PAID",
                    System.currentTimeMillis()
            );
            var expectedResponse = new LedgerResponse(List.of(contribution));
            when(getLedgerHandler.handle(any(GetLedger.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributions").isArray())
                    .andExpect(jsonPath("$.contributions[0].id").value("contrib-123"))
                    .andExpect(jsonPath("$.contributions[0].caseCode").value(12345))
                    .andExpect(jsonPath("$.contributions[0].amount").value(5000))
                    .andExpect(jsonPath("$.contributions[0].status").value("PAID"));

            verify(getLedgerHandler).handle(any(GetLedger.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass correct userId to handler")
        void shouldPassCorrectUserIdToHandler() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(getLedgerHandler.handle(any(GetLedger.class)))
                    .thenReturn(new LedgerResponse(List.of()));

            // Act
            mockMvc.perform(get("/v1/ledger/{userId}", userId))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetLedger> captor = ArgumentCaptor.forClass(GetLedger.class);
            verify(getLedgerHandler).handle(captor.capture());
            GetLedger captured = captor.getValue();

            assertThat(captured.userId()).isEqualTo(userId);
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should return empty list when no contributions")
        void shouldReturnEmptyListWhenNoContributions() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(getLedgerHandler.handle(any(GetLedger.class)))
                    .thenReturn(new LedgerResponse(List.of()));

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributions").isArray())
                    .andExpect(jsonPath("$.contributions").isEmpty());
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should return multiple contributions")
        void shouldReturnMultipleContributions() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            var contrib1 = new Contribution("id1", userId.toString(), 1, "Case 1", 1000, "PAID", System.currentTimeMillis());
            var contrib2 = new Contribution("id2", userId.toString(), 2, "Case 2", 2000, "PLEDGED", System.currentTimeMillis());
            when(getLedgerHandler.handle(any(GetLedger.class)))
                    .thenReturn(new LedgerResponse(List.of(contrib1, contrib2)));

            // Act & Assert
            mockMvc.perform(get("/v1/ledger/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contributions[0].id").value("id1"))
                    .andExpect(jsonPath("$.contributions[1].id").value("id2"));
        }
    }
}
