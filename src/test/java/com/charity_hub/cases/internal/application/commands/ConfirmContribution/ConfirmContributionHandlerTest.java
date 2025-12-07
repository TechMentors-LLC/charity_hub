package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Confirm Contribution Handler Tests")
class ConfirmContributionHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @Mock
    private ILogger logger;

    @Mock
    private Contribution contribution;

    private ConfirmContributionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ConfirmContributionHandler(caseRepo, logger);
    }

    @Test
    @DisplayName("Should confirm contribution successfully")
    void shouldConfirmContribution() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));

        // When
        handler.handle(command);

        // Then
        verify(contribution).confirm();
        verify(caseRepo).save(contribution);
        verify(logger).info("Contribution confirmed and saved with ID {}", contributionId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when contribution not found")
    void shouldThrowNotFoundExceptionWhenContributionNotFound() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Contribution not found with ID " + contributionId);

        verify(caseRepo).getContributionById(contributionId);
        verify(logger).error("Contribution not found with ID {} ", contributionId);
        verify(contribution, never()).confirm();
        verify(caseRepo, never()).save(any(Contribution.class));
    }

    @Test
    @DisplayName("Should handle repository errors gracefully")
    void shouldHandleRepositoryErrors() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId);

        when(caseRepo.getContributionById(contributionId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(contribution, never()).confirm();
        verify(caseRepo, never()).save(any(Contribution.class));
    }
}
