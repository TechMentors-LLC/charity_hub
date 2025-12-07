package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCases;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesHandler;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesResponse;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetDraftCasesController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GetDraftCasesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetDraftCasesHandler getDraftCasesHandler;

    @Nested
    @DisplayName("GET /v1/draft-cases")
    class GetDraftCasesEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should get draft cases successfully and return 200")
        void shouldGetDraftCasesSuccessfully() throws Exception {
            // Arrange
            var draftCase1 = new GetDraftCasesResponse.DraftCase(
                    1, "Draft Case 1", "Description 1", 10000,
                    System.currentTimeMillis(), System.currentTimeMillis(), List.of()
            );
            var draftCase2 = new GetDraftCasesResponse.DraftCase(
                    2, "Draft Case 2", "Description 2", 20000,
                    System.currentTimeMillis(), System.currentTimeMillis(), List.of("doc.pdf")
            );
            var expectedResponse = new GetDraftCasesResponse(List.of(draftCase1, draftCase2));
            when(getDraftCasesHandler.handle(any(GetDraftCases.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/draft-cases"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cases").isArray())
                    .andExpect(jsonPath("$.cases[0].code").value(1))
                    .andExpect(jsonPath("$.cases[0].title").value("Draft Case 1"))
                    .andExpect(jsonPath("$.cases[1].code").value(2))
                    .andExpect(jsonPath("$.cases[1].title").value("Draft Case 2"));

            verify(getDraftCasesHandler).handle(any(GetDraftCases.class));
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty list when no draft cases")
        void shouldReturnEmptyListWhenNoDraftCases() throws Exception {
            // Arrange
            var expectedResponse = new GetDraftCasesResponse(List.of());
            when(getDraftCasesHandler.handle(any(GetDraftCases.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/draft-cases"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cases").isArray())
                    .andExpect(jsonPath("$.cases").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("should return draft case with documents")
        void shouldReturnDraftCaseWithDocuments() throws Exception {
            // Arrange
            var draftCase = new GetDraftCasesResponse.DraftCase(
                    5, "Case with Docs", "Description", 50000,
                    System.currentTimeMillis(), System.currentTimeMillis(),
                    List.of("doc1.pdf", "doc2.pdf", "doc3.pdf")
            );
            var expectedResponse = new GetDraftCasesResponse(List.of(draftCase));
            when(getDraftCasesHandler.handle(any(GetDraftCases.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/draft-cases"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cases[0].documents").isArray())
                    .andExpect(jsonPath("$.cases[0].documents[0]").value("doc1.pdf"))
                    .andExpect(jsonPath("$.cases[0].documents[1]").value("doc2.pdf"))
                    .andExpect(jsonPath("$.cases[0].documents[2]").value("doc3.pdf"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return draft case details correctly")
        void shouldReturnDraftCaseDetailsCorrectly() throws Exception {
            // Arrange
            long creationDate = 1700000000000L;
            long lastUpdated = 1700100000000L;
            var draftCase = new GetDraftCasesResponse.DraftCase(
                    100, "Detailed Case", "Detailed Description", 75000,
                    creationDate, lastUpdated, List.of()
            );
            var expectedResponse = new GetDraftCasesResponse(List.of(draftCase));
            when(getDraftCasesHandler.handle(any(GetDraftCases.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(get("/v1/draft-cases"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cases[0].code").value(100))
                    .andExpect(jsonPath("$.cases[0].title").value("Detailed Case"))
                    .andExpect(jsonPath("$.cases[0].description").value("Detailed Description"))
                    .andExpect(jsonPath("$.cases[0].goal").value(75000))
                    .andExpect(jsonPath("$.cases[0].creationDate").value(creationDate))
                    .andExpect(jsonPath("$.cases[0].lastUpdated").value(lastUpdated));
        }
    }
}
