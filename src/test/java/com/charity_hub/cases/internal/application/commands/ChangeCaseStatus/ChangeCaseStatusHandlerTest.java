package com.charity_hub.cases.internal.application.commands.ChangeCaseStatus;

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
@DisplayName("ChangeCaseStatusHandler Tests")
class ChangeCaseStatusHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @InjectMocks
    private ChangeCaseStatusHandler handler;

    private final int CASE_CODE = 12345;

    @Test
    @DisplayName("Should open case when action is open")
    void shouldOpenCaseWhenActionIsOpen() {
        Case case_ = mock(Case.class);
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.of(case_));

        ChangeCaseStatus command = new ChangeCaseStatus(CASE_CODE, true);
        handler.handle(command);

        verify(case_).open();
        verify(case_, never()).close();
        verify(caseRepo).save(case_);
    }

    @Test
    @DisplayName("Should close case when action is close")
    void shouldCloseCaseWhenActionIsClose() {
        Case case_ = mock(Case.class);
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.of(case_));

        ChangeCaseStatus command = new ChangeCaseStatus(CASE_CODE, false);
        handler.handle(command);

        verify(case_).close();
        verify(case_, never()).open();
        verify(caseRepo).save(case_);
    }

    @Test
    @DisplayName("Should throw NotFoundException when case not found")
    void shouldThrowNotFoundWhenCaseNotFound() {
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.empty());

        ChangeCaseStatus command = new ChangeCaseStatus(CASE_CODE, true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }
}
