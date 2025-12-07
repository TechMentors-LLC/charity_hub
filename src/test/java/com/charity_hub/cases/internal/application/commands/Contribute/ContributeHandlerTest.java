package com.charity_hub.cases.internal.application.commands.Contribute;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributeHandler Tests")
class ContributeHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @InjectMocks
    private ContributeHandler handler;

    private final int CASE_CODE = 12345;
    private final UUID USER_ID = UUID.randomUUID();
    private final int AMOUNT = 1000;

    @Test
    @DisplayName("Should create contribution and return contribution ID")
    void shouldCreateContributionAndReturnContributionId() {
        Case case_ = mock(Case.class);
        Contribution contribution = mock(Contribution.class);
        String contributionId = "contribution-id-123";

        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.of(case_));
        when(case_.contribute(eq(USER_ID), eq(AMOUNT))).thenReturn(contribution);
        when(contribution.contributionId()).thenReturn(contributionId);

        Contribute command = new Contribute(AMOUNT, USER_ID, CASE_CODE);
        ContributeDefaultResponse response = handler.handle(command);

        assertThat(response.getContributionId()).isEqualTo(contributionId);
        verify(caseRepo).save(case_);
    }

    @Test
    @DisplayName("Should throw NotFoundException when case not found")
    void shouldThrowNotFoundWhenCaseNotFound() {
        when(caseRepo.getByCode(any(CaseCode.class))).thenReturn(Optional.empty());

        Contribute command = new Contribute(AMOUNT, USER_ID, CASE_CODE);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class);
    }
}
