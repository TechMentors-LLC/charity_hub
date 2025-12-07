package com.charity_hub.cases.internal.application.commands.DeleteDraftCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteDraftCaseHandler Tests")
class DeleteDraftCaseHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @InjectMocks
    private DeleteDraftCaseHandler handler;

    private final int CASE_CODE = 12345;

    @Test
    @DisplayName("Should delete draft case")
    void shouldDeleteDraftCase() {
        Case case_ = mock(Case.class);
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.of(case_));

        DeleteDraftCase command = new DeleteDraftCase(CASE_CODE);
        handler.handle(command);

        verify(case_).delete(caseRepo);
    }

    @Test
    @DisplayName("Should throw NotFoundException when case not found")
    void shouldThrowNotFoundWhenCaseNotFound() {
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.empty());

        DeleteDraftCase command = new DeleteDraftCase(CASE_CODE);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }
}
