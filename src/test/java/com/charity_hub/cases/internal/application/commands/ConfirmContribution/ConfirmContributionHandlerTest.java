package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.cases.shared.IMemberGateway;
import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.infrastructure.gateways.AccountsGateway;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import com.charity_hub.shared.exceptions.UnAuthorized;
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
    private IMemberGateway memberGateway;

    @Mock
    private AccountsGateway accountsGateway;

    @Mock
    private ILogger logger;

    @Mock
    private Contribution contribution;

    private ConfirmContributionHandler handler;
    private final UUID userId = UUID.randomUUID();
    private final UUID contributorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new ConfirmContributionHandler(caseRepo, memberGateway, accountsGateway, logger);
    }

    @Test
    @DisplayName("Should confirm contribution successfully when user is parent")
    void shouldConfirmContributionWhenParent() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId, userId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));
        when(contribution.getContributorId()).thenReturn(contributorId);
        when(memberGateway.isParent(userId, contributorId)).thenReturn(true);

        // When
        handler.handle(command);

        // Then
        verify(contribution).confirm();
        verify(caseRepo).save(contribution);
        verify(logger).info("Contribution confirmed and saved with ID {}", contributionId);
    }

    @Test
    @DisplayName("Should confirm contribution successfully when user is admin")
    void shouldConfirmContributionWhenAdmin() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId, userId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));
        when(contribution.getContributorId()).thenReturn(contributorId);
        when(memberGateway.isParent(userId, contributorId)).thenReturn(false);
        when(accountsGateway.isAdmin(userId)).thenReturn(true);

        // When
        handler.handle(command);

        // Then
        verify(contribution).confirm();
        verify(caseRepo).save(contribution);
    }

    @Test
    @DisplayName("Should throw UnAuthorized when user is neither parent nor admin")
    void shouldThrowUnAuthorizedWhenNotAuthorized() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId, userId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.of(contribution));
        when(contribution.getContributorId()).thenReturn(contributorId);
        when(memberGateway.isParent(userId, contributorId)).thenReturn(false);
        when(accountsGateway.isAdmin(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(UnAuthorized.class)
                .hasMessageContaining("Only parent or admin can confirm contribution");

        verify(contribution, never()).confirm();
        verify(caseRepo, never()).save(any(Contribution.class));
    }

    @Test
    @DisplayName("Should throw NotFoundException when contribution not found")
    void shouldThrowNotFoundExceptionWhenContributionNotFound() {
        // Given
        UUID contributionId = UUID.randomUUID();
        ConfirmContribution command = new ConfirmContribution(contributionId, userId);

        when(caseRepo.getContributionById(contributionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Contribution not found with ID " + contributionId);

        verify(caseRepo).getContributionById(contributionId);
        verify(logger).error("Contribution not found with ID {} ", contributionId);
        verify(contribution, never()).confirm();
    }
}
