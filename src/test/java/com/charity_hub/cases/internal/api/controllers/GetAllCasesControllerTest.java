package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetAllCases.GetAllCasesQuery;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetCasesQueryResult;
import com.charity_hub.cases.internal.infrastructure.queryhandlers.GetAllCasesHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetAllCasesController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GetAllCasesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAllCasesHandler getAllCasesHandler;

    @Nested
    @DisplayName("GET /v1/cases")
    class GetAllCasesEndpoint {

        @Test
        @WithMockUser
        @DisplayName("should get all cases successfully and return 200")
        void shouldGetAllCasesSuccessfully() throws Exception {
            // Arrange
            var case1 = new GetCasesQueryResult.Case(
                    1, "Case 1", "Description 1", 10000, 5000, true, "OPEN",
                    System.currentTimeMillis(), System.currentTimeMillis(), List.of()
            );
            var case2 = new GetCasesQueryResult.Case(
                    2, "Case 2", "Description 2", 20000, 10000, false, "CLOSED",
                    System.currentTimeMillis(), System.currentTimeMillis(), List.of("doc.pdf")
            );
            var expectedResult = new GetCasesQueryResult(List.of(case1, case2), 2);
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class))).thenReturn(expectedResult);

            // Act & Assert
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(2))
                    .andExpect(jsonPath("$.cases").isArray())
                    .andExpect(jsonPath("$.cases[0].code").value(1))
                    .andExpect(jsonPath("$.cases[1].code").value(2));

            verify(getAllCasesHandler).handle(any(GetAllCasesQuery.class));
        }

        @Test
        @WithMockUser
        @DisplayName("should pass pagination parameters correctly")
        void shouldPassPaginationParametersCorrectly() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "10")
                            .param("limit", "50"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetAllCasesQuery> captor = ArgumentCaptor.forClass(GetAllCasesQuery.class);
            verify(getAllCasesHandler).handle(captor.capture());
            GetAllCasesQuery captured = captor.getValue();

            assertThat(captured.offset()).isEqualTo(10);
            assertThat(captured.limit()).isEqualTo(50);
        }

        @Test
        @WithMockUser
        @DisplayName("should clamp limit to maximum of 100")
        void shouldClampLimitToMaximum() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "0")
                            .param("limit", "500"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetAllCasesQuery> captor = ArgumentCaptor.forClass(GetAllCasesQuery.class);
            verify(getAllCasesHandler).handle(captor.capture());
            GetAllCasesQuery captured = captor.getValue();

            assertThat(captured.limit()).isEqualTo(100);
        }

        @Test
        @WithMockUser
        @DisplayName("should clamp offset to minimum of 0")
        void shouldClampOffsetToMinimum() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "-10")
                            .param("limit", "10"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetAllCasesQuery> captor = ArgumentCaptor.forClass(GetAllCasesQuery.class);
            verify(getAllCasesHandler).handle(captor.capture());
            GetAllCasesQuery captured = captor.getValue();

            assertThat(captured.offset()).isEqualTo(0);
        }

        @Test
        @WithMockUser
        @DisplayName("should clamp limit to minimum of 1")
        void shouldClampLimitToMinimum() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "0")
                            .param("limit", "0"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetAllCasesQuery> captor = ArgumentCaptor.forClass(GetAllCasesQuery.class);
            verify(getAllCasesHandler).handle(captor.capture());
            GetAllCasesQuery captured = captor.getValue();

            assertThat(captured.limit()).isEqualTo(1);
        }

        @Test
        @WithMockUser
        @DisplayName("should pass filter parameters correctly")
        void shouldPassFilterParametersCorrectly() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "0")
                            .param("limit", "10")
                            .param("code", "12345")
                            .param("tag", "urgent")
                            .param("content", "medical"))
                    .andExpect(status().isOk());

            // Assert
            ArgumentCaptor<GetAllCasesQuery> captor = ArgumentCaptor.forClass(GetAllCasesQuery.class);
            verify(getAllCasesHandler).handle(captor.capture());
            GetAllCasesQuery captured = captor.getValue();

            assertThat(captured.code()).isEqualTo(12345);
            assertThat(captured.tag()).isEqualTo("urgent");
            assertThat(captured.content()).isEqualTo("medical");
        }

        @Test
        @WithMockUser
        @DisplayName("should return empty list when no cases found")
        void shouldReturnEmptyListWhenNoCasesFound() throws Exception {
            // Arrange
            when(getAllCasesHandler.handle(any(GetAllCasesQuery.class)))
                    .thenReturn(new GetCasesQueryResult(List.of(), 0));

            // Act & Assert
            mockMvc.perform(get("/v1/cases")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").value(0))
                    .andExpect(jsonPath("$.cases").isArray())
                    .andExpect(jsonPath("$.cases").isEmpty());
        }
    }
}
