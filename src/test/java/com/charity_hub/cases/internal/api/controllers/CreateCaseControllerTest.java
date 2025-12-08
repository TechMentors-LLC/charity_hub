package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.CreateCaseRequest;
import com.charity_hub.cases.internal.application.commands.CreateCase.CaseResponse;
import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCase;
import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCaseHandler;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateCaseController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CreateCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateCaseHandler createCaseHandler;

    @MockBean
    private BusinessMetrics businessMetrics;

    @Nested
    @DisplayName("POST /v1/cases")
    class CreateCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"CREATE_CASES"})
        @DisplayName("should create case successfully and return 201")
        void shouldCreateCaseSuccessfully() throws Exception {
            // Arrange
            var request = new CreateCaseRequest(
                    "Test Case",
                    "Test Description",
                    10000,
                    true,
                    true,
                    List.of("doc1.pdf", "doc2.pdf")
            );
            var expectedResponse = new CaseResponse(12345);
            when(createCaseHandler.handle(any(CreateCase.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post("/v1/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.caseCode").value(12345));

            verify(createCaseHandler).handle(any(CreateCase.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should allow FULL_ACCESS authority to create case")
        void shouldAllowFullAccessToCreateCase() throws Exception {
            // Arrange
            var request = new CreateCaseRequest(
                    "Another Case",
                    "Another Description",
                    5000,
                    false,
                    false,
                    List.of()
            );
            var expectedResponse = new CaseResponse(67890);
            when(createCaseHandler.handle(any(CreateCase.class))).thenReturn(expectedResponse);

            // Act & Assert
            mockMvc.perform(post("/v1/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.caseCode").value(67890));
        }

        @Test
        @WithMockUser(authorities = {"CREATE_CASES"})
        @DisplayName("should pass correct parameters to handler")
        void shouldPassCorrectParametersToHandler() throws Exception {
            // Arrange
            var request = new CreateCaseRequest(
                    "Charity Case",
                    "Helping people",
                    20000,
                    true,
                    false,
                    List.of("evidence.pdf")
            );
            when(createCaseHandler.handle(any(CreateCase.class))).thenReturn(new CaseResponse(1));

            // Act
            mockMvc.perform(post("/v1/cases")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Assert
            ArgumentCaptor<CreateCase> captor = ArgumentCaptor.forClass(CreateCase.class);
            verify(createCaseHandler).handle(captor.capture());
            CreateCase captured = captor.getValue();

            assertThat(captured.title()).isEqualTo("Charity Case");
            assertThat(captured.description()).isEqualTo("Helping people");
            assertThat(captured.goal()).isEqualTo(20000);
            assertThat(captured.publish()).isTrue();
            assertThat(captured.acceptZakat()).isFalse();
            assertThat(captured.documents()).containsExactly("evidence.pdf");
        }
    }
}
