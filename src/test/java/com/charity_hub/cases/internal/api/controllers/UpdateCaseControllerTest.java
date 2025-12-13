package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.UpdateCaseRequest;
import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCase;
import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCaseHandler;
import com.charity_hub.shared.exceptions.GlobalExceptionHandler;
import com.charity_hub.shared.observability.TestObservabilityConfiguration;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UpdateCaseController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class UpdateCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UpdateCaseHandler updateCaseHandler;

    @Nested
    @DisplayName("PUT /v1/cases/{caseCode}")
    class UpdateCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should update case successfully and return 200")
        void shouldUpdateCaseSuccessfully() throws Exception {
            // Arrange
            var request = new UpdateCaseRequest(
                    "Updated Title",
                    "Updated Description",
                    15000,
                    true,
                    List.of("new_doc.pdf")
            );
            doNothing().when(updateCaseHandler).handle(any(UpdateCase.class));

            // Act & Assert
            mockMvc.perform(put("/v1/cases/12345")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(updateCaseHandler).handle(any(UpdateCase.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass case code and request data to handler")
        void shouldPassCorrectParametersToHandler() throws Exception {
            // Arrange
            var request = new UpdateCaseRequest(
                    "Modified Case",
                    "Modified Description",
                    25000,
                    false,
                    List.of("doc1.pdf", "doc2.pdf")
            );
            doNothing().when(updateCaseHandler).handle(any(UpdateCase.class));

            // Act
            mockMvc.perform(put("/v1/cases/98765")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<UpdateCase> captor = ArgumentCaptor.forClass(UpdateCase.class);
            verify(updateCaseHandler).handle(captor.capture());
            UpdateCase captured = captor.getValue();

            assertThat(captured.caseCode()).isEqualTo(98765);
            assertThat(captured.title()).isEqualTo("Modified Case");
            assertThat(captured.description()).isEqualTo("Modified Description");
            assertThat(captured.goal()).isEqualTo(25000);
            assertThat(captured.acceptZakat()).isFalse();
            assertThat(captured.documents()).containsExactly("doc1.pdf", "doc2.pdf");
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should update case with empty documents list")
        void shouldUpdateCaseWithEmptyDocuments() throws Exception {
            // Arrange
            var request = new UpdateCaseRequest(
                    "Simple Case",
                    "Simple Description",
                    5000,
                    true,
                    List.of()
            );
            doNothing().when(updateCaseHandler).handle(any(UpdateCase.class));

            // Act & Assert
            mockMvc.perform(put("/v1/cases/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(updateCaseHandler).handle(any(UpdateCase.class));
        }
    }
}
