package com.charity_hub.cases.internal.application.commands.PayContribution;

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
@DisplayName("Pay Contribution Handler Tests")
class PayContributionHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @Mock
    private ILogger logger;

    @Mock
    private Contribution contribution;

    private PayContributionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PayContributionHandler(caseRepo, logger);
    }

    @Test
    @DisplayName("Should pay contribution with payment proof")
    void shouldPayContributionWithProof() {
        // Given
        UUID contributionId = UUID.randomUUID();
        String paymentProof = "https://example.com/proof.jpg";
        PayContribution command = new PayContribution(contributionId, paymentProof);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));

        // When
        handler.handle(command);

        // Then
        verify(contribution).pay(paymentProof);
        verify(caseRepo).save(contribution);
        verify(logger).info("Contribution paid and saved with ID {} ", contributionId);
    }

    @Test
    @DisplayName("Should pay contribution without payment proof")
    void shouldPayContributionWithoutProof() {
        // Given
        UUID contributionId = UUID.randomUUID();
        PayContribution command = new PayContribution(contributionId, null);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));

        // When
        handler.handle(command);

        // Then
        verify(contribution).pay(null);
        verify(caseRepo).save(contribution);
        verify(logger).info("Contribution paid and saved with ID {} ", contributionId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when contribution not found")
    void shouldThrowNotFoundExceptionWhenContributionNotFound() {
        // Given
        UUID contributionId = UUID.randomUUID();
        String paymentProof = "https://example.com/proof.jpg";
        PayContribution command = new PayContribution(contributionId, paymentProof);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Contribution not found with ID " + contributionId);

        verify(caseRepo).getContributionById(contributionId);
        verify(logger).error("Contribution not found with ID {} ", contributionId);
        verify(contribution, never()).pay(anyString());
        verify(caseRepo, never()).save(any(Contribution.class));
    }

    @Test
    @DisplayName("Should handle repository errors gracefully")
    void shouldHandleRepositoryErrors() {
        // Given
        UUID contributionId = UUID.randomUUID();
        PayContribution command = new PayContribution(contributionId, "proof");

        when(caseRepo.getContributionById(contributionId))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(contribution, never()).pay(anyString());
        verify(caseRepo, never()).save(any(Contribution.class));
    }
}
