package com.charity_hub.cases.internal.infrastructure.queryhandlers;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetAllCasesQuery;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetCasesQueryResult;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllCasesHandler Tests")
class GetAllCasesHandlerTest {

    @Mock
    private ICaseReadRepo caseRepo;

    private GetAllCasesHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetAllCasesHandler(caseRepo);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should return cases with count")
        void shouldReturnCasesWithCount() {
            // Arrange
            var query = new GetAllCasesQuery(null, null, null, 0, 10, false);
            var caseEntity1 = createCaseEntity(1, "Case 1", "OPEN");
            var caseEntity2 = createCaseEntity(2, "Case 2", "CLOSED");

            when(caseRepo.search(anyInt(), anyInt(), any())).thenReturn(List.of(caseEntity1, caseEntity2));
            when(caseRepo.getCasesCount(any())).thenReturn(2);

            // Act
            GetCasesQueryResult result = handler.handle(query);

            // Assert
            assertThat(result.count()).isEqualTo(2);
            assertThat(result.cases()).hasSize(2);
            assertThat(result.cases().get(0).code()).isEqualTo(1);
            assertThat(result.cases().get(1).code()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return empty list when no cases found")
        void shouldReturnEmptyListWhenNoCasesFound() {
            // Arrange
            var query = new GetAllCasesQuery(null, null, null, 0, 10, false);
            when(caseRepo.search(anyInt(), anyInt(), any())).thenReturn(List.of());
            when(caseRepo.getCasesCount(any())).thenReturn(0);

            // Act
            GetCasesQueryResult result = handler.handle(query);

            // Assert
            assertThat(result.count()).isEqualTo(0);
            assertThat(result.cases()).isEmpty();
        }

        @Test
        @DisplayName("should pass offset and limit to repository")
        void shouldPassOffsetAndLimitToRepository() {
            // Arrange
            var query = new GetAllCasesQuery(null, null, null, 10, 20, false);
            when(caseRepo.search(anyInt(), anyInt(), any())).thenReturn(List.of());
            when(caseRepo.getCasesCount(any())).thenReturn(0);

            // Act
            handler.handle(query);

            // Assert
            verify(caseRepo).search(anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("should map case entity to query result correctly")
        void shouldMapCaseEntityToQueryResultCorrectly() {
            // Arrange
            var query = new GetAllCasesQuery(null, null, null, 0, 10, false);
            long creationDate = System.currentTimeMillis();
            long lastUpdated = System.currentTimeMillis() + 1000;
            var caseEntity = new CaseEntity(
                    100,
                    "Test Title",
                    "Test Description",
                    50000,
                    25000,
                    CaseEntity.STATUS_OPENED,
                    true,
                    creationDate,
                    lastUpdated,
                    List.of(),
                    List.of("doc1.pdf"),
                    0);

            when(caseRepo.search(anyInt(), anyInt(), any())).thenReturn(List.of(caseEntity));
            when(caseRepo.getCasesCount(any())).thenReturn(1);

            // Act
            GetCasesQueryResult result = handler.handle(query);

            // Assert
            var case_ = result.cases().get(0);
            assertThat(case_.code()).isEqualTo(100);
            assertThat(case_.title()).isEqualTo("Test Title");
            assertThat(case_.description()).isEqualTo("Test Description");
            assertThat(case_.goal()).isEqualTo(50000);
            assertThat(case_.collected()).isEqualTo(25000);
            assertThat(case_.acceptZakat()).isTrue();
            assertThat(case_.status()).isEqualTo("OPENED");
        }

        @Test
        @DisplayName("should filter by code when provided")
        void shouldFilterByCodeWhenProvided() {
            // Arrange
            var query = new GetAllCasesQuery(12345, null, null, 0, 10, false);
            when(caseRepo.search(anyInt(), anyInt(), any())).thenReturn(List.of());
            when(caseRepo.getCasesCount(any())).thenReturn(0);

            // Act
            handler.handle(query);

            // Assert
            verify(caseRepo).search(anyInt(), anyInt(), any());
            verify(caseRepo).getCasesCount(any());
        }

        private CaseEntity createCaseEntity(int code, String title, String status) {
            int statusCode = switch (status) {
                case "DRAFT" -> CaseEntity.STATUS_DRAFT;
                case "OPENED" -> CaseEntity.STATUS_OPENED;
                case "CLOSED" -> CaseEntity.STATUS_CLOSED;
                default -> CaseEntity.STATUS_OPENED;
            };
            return new CaseEntity(
                    code,
                    title,
                    "Description for " + title,
                    10000,
                    5000,
                    statusCode,
                    true,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    List.of(),
                    List.of(),
                    0);
        }
    }
}
