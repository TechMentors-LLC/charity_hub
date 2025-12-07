package com.charity_hub.cases.internal.application.queries.GetDraftCases;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDraftCasesHandler Tests")
class GetDraftCasesHandlerTest {

    @Mock
    private ICaseReadRepo caseRepo;

    private GetDraftCasesHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetDraftCasesHandler(caseRepo);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should return draft cases")
        void shouldReturnDraftCases() {
            // Arrange
            var query = new GetDraftCases();
            var caseEntity1 = createCaseEntity(1, "Draft Case 1", "Description 1", 10000);
            var caseEntity2 = createCaseEntity(2, "Draft Case 2", "Description 2", 20000);
            when(caseRepo.getDraftCases()).thenReturn(List.of(caseEntity1, caseEntity2));

            // Act
            GetDraftCasesResponse result = handler.handle(query);

            // Assert
            assertThat(result.cases()).hasSize(2);
            assertThat(result.cases().get(0).code()).isEqualTo(1);
            assertThat(result.cases().get(0).title()).isEqualTo("Draft Case 1");
            assertThat(result.cases().get(1).code()).isEqualTo(2);
            assertThat(result.cases().get(1).title()).isEqualTo("Draft Case 2");
            verify(caseRepo).getDraftCases();
        }

        @Test
        @DisplayName("should return empty list when no draft cases")
        void shouldReturnEmptyListWhenNoDraftCases() {
            // Arrange
            var query = new GetDraftCases();
            when(caseRepo.getDraftCases()).thenReturn(List.of());

            // Act
            GetDraftCasesResponse result = handler.handle(query);

            // Assert
            assertThat(result.cases()).isEmpty();
            verify(caseRepo).getDraftCases();
        }

        @Test
        @DisplayName("should map case entity fields correctly")
        void shouldMapCaseEntityFieldsCorrectly() {
            // Arrange
            var query = new GetDraftCases();
            long creationDate = System.currentTimeMillis();
            long lastUpdated = System.currentTimeMillis() + 1000;
            var caseEntity = new CaseEntity(
                    1,
                    "Test Title",
                    "Test Description",
                    50000,
                    0,
                    CaseEntity.STATUS_DRAFT,
                    true,
                    creationDate,
                    lastUpdated,
                    List.of(),
                    List.of("doc1.pdf", "doc2.pdf"),
                    0
            );
            when(caseRepo.getDraftCases()).thenReturn(List.of(caseEntity));

            // Act
            GetDraftCasesResponse result = handler.handle(query);

            // Assert
            var draftCase = result.cases().get(0);
            assertThat(draftCase.code()).isEqualTo(1);
            assertThat(draftCase.title()).isEqualTo("Test Title");
            assertThat(draftCase.description()).isEqualTo("Test Description");
            assertThat(draftCase.goal()).isEqualTo(50000);
            assertThat(draftCase.creationDate()).isEqualTo(creationDate);
            assertThat(draftCase.lastUpdated()).isEqualTo(lastUpdated);
            assertThat(draftCase.documents()).containsExactly("doc1.pdf", "doc2.pdf");
        }

        private CaseEntity createCaseEntity(int code, String title, String description, int goal) {
            return new CaseEntity(
                    code,
                    title,
                    description,
                    goal,
                    0,
                    CaseEntity.STATUS_DRAFT,
                    true,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    List.of(),
                    List.of(),
                    0
            );
        }
    }
}
