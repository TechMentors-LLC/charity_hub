package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ChangeCaseStatus.ChangeCaseStatus;
import com.charity_hub.cases.internal.application.commands.ChangeCaseStatus.ChangeCaseStatusHandler;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChangeCaseStatusController.class)
@Import({GlobalExceptionHandler.class, TestObservabilityConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
class ChangeCaseStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChangeCaseStatusHandler changeCaseStatusHandler;

    @Nested
    @DisplayName("POST /v1/cases/{caseCode}/open")
    class OpenCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should open case successfully and return 200")
        void shouldOpenCaseSuccessfully() throws Exception {
            // Arrange
            doNothing().when(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));

            // Act & Assert
            mockMvc.perform(post("/v1/cases/12345/open"))
                    .andExpect(status().isOk());

            verify(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass correct parameters for open action")
        void shouldPassCorrectParametersForOpen() throws Exception {
            // Arrange
            doNothing().when(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));

            // Act
            mockMvc.perform(post("/v1/cases/54321/open"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<ChangeCaseStatus> captor = ArgumentCaptor.forClass(ChangeCaseStatus.class);
            verify(changeCaseStatusHandler).handle(captor.capture());
            ChangeCaseStatus captured = captor.getValue();

            assertThat(captured.caseCode()).isEqualTo(54321);
            assertThat(captured.isActionOpen()).isTrue();
        }
    }

    @Nested
    @DisplayName("POST /v1/cases/{caseCode}/close")
    class CloseCaseEndpoint {

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should close case successfully and return 200")
        void shouldCloseCaseSuccessfully() throws Exception {
            // Arrange
            doNothing().when(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));

            // Act & Assert
            mockMvc.perform(post("/v1/cases/12345/close"))
                    .andExpect(status().isOk());

            verify(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));
        }

        @Test
        @WithMockUser(authorities = {"FULL_ACCESS"})
        @DisplayName("should pass correct parameters for close action")
        void shouldPassCorrectParametersForClose() throws Exception {
            // Arrange
            doNothing().when(changeCaseStatusHandler).handle(any(ChangeCaseStatus.class));

            // Act
            mockMvc.perform(post("/v1/cases/99999/close"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<ChangeCaseStatus> captor = ArgumentCaptor.forClass(ChangeCaseStatus.class);
            verify(changeCaseStatusHandler).handle(captor.capture());
            ChangeCaseStatus captured = captor.getValue();

            assertThat(captured.caseCode()).isEqualTo(99999);
            assertThat(captured.isActionOpen()).isFalse();
        }
    }
}
