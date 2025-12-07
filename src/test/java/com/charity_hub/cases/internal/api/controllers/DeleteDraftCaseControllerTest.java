package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCase;
import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCaseHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeleteDraftCaseController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class DeleteDraftCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeleteDraftCaseHandler deleteDraftCaseHandler;

    @Nested
    @DisplayName("DELETE /v1/cases/{caseCode}")
    class DeleteDraftCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should delete draft case successfully and return 200")
        void shouldDeleteDraftCaseSuccessfully() throws Exception {
            // Arrange
            doNothing().when(deleteDraftCaseHandler).handle(any(DeleteDraftCase.class));

            // Act & Assert
            mockMvc.perform(delete("/v1/cases/12345"))
                    .andExpect(status().isOk());

            verify(deleteDraftCaseHandler).handle(any(DeleteDraftCase.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass correct case code to handler")
        void shouldPassCorrectCaseCodeToHandler() throws Exception {
            // Arrange
            doNothing().when(deleteDraftCaseHandler).handle(any(DeleteDraftCase.class));

            // Act
            mockMvc.perform(delete("/v1/cases/98765"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<DeleteDraftCase> captor = ArgumentCaptor.forClass(DeleteDraftCase.class);
            verify(deleteDraftCaseHandler).handle(captor.capture());
            DeleteDraftCase captured = captor.getValue();

            assertThat(captured.caseCode()).isEqualTo(98765);
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should handle different case codes")
        void shouldHandleDifferentCaseCodes() throws Exception {
            // Arrange
            doNothing().when(deleteDraftCaseHandler).handle(any(DeleteDraftCase.class));

            // Act & Assert - Test with edge case numbers
            mockMvc.perform(delete("/v1/cases/1"))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/v1/cases/999999"))
                    .andExpect(status().isOk());
        }
    }
}
