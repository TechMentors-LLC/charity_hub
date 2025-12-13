package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionMadeLogger;
import com.charity_hub.ledger.internal.domain.model.*;
import com.charity_hub.shared.domain.IEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributionMadeHandler Tests")
class ContributionMadeHandlerTest {

    @Mock
    private IEventBus eventBus;

    @Mock
    private ILedgerRepository ledgerRepository;

    @Mock
    private IMembersNetworkRepo membersNetworkRepo;

    @Mock
    private ContributionMadeLogger logger;

    private ContributionMadeHandler handler;

    private final UUID CONTRIBUTOR_ID = UUID.randomUUID();
    private final UUID CONTRIBUTION_ID = UUID.randomUUID();
    private final UUID PARENT_ID = UUID.randomUUID();
    private final UUID GRANDPARENT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new ContributionMadeHandler(eventBus, ledgerRepository, membersNetworkRepo, logger);
    }

    @Nested
    @DisplayName("When handler starts")
    class HandlerRegistration {

        @Test
        @DisplayName("Should register handler and subscribe to ContributionMadeDTO events")
        void shouldRegisterHandler() {
            handler.start();

            verify(logger).handlerRegistered();
            verify(eventBus).subscribe(eq(handler), eq(ContributionMadeDTO.class), any());
        }
    }

    @Nested
    @DisplayName("When processing contribution made event")
    class ProcessingContributionMade {

        @Test
        @DisplayName("Should credit contributor's dueAmount and dueNetworkAmount")
        void shouldCreditContributorLedgerAmounts() {
            // Given
            int contributionAmount = 100;
            ContributionMadeDTO contribution = createContributionMadeDTO(contributionAmount);

            Member contributorMember = new Member(
                    new MemberId(CONTRIBUTOR_ID),
                    new MemberId(PARENT_ID),
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

            MemberId memberId = new MemberId(CONTRIBUTOR_ID);
            Ledger contributorLedger = Ledger.createNew(memberId);
            when(ledgerRepository.findByMemberId(memberId)).thenReturn(contributorLedger);

            // When
            invokeHandle(contribution);

            // Then
            ArgumentCaptor<Ledger> ledgerCaptor = ArgumentCaptor.forClass(Ledger.class);
            verify(ledgerRepository).save(ledgerCaptor.capture());

            Ledger savedLedger = ledgerCaptor.getValue();
            assertThat(savedLedger.getDueAmount().value()).isEqualTo(contributionAmount);
            assertThat(savedLedger.getDueNetworkAmount().value()).isEqualTo(contributionAmount);
        }

        @Test
        @DisplayName("Should cascade dueNetworkAmount to all ancestors")
        void shouldCascadeToAncestors() {
            // Given
            int contributionAmount = 100;
            ContributionMadeDTO contribution = createContributionMadeDTO(contributionAmount);

            List<MemberId> ancestors = List.of(new MemberId(PARENT_ID), new MemberId(GRANDPARENT_ID));
            Member contributorMember = new Member(
                    new MemberId(CONTRIBUTOR_ID),
                    new MemberId(PARENT_ID),
                    ancestors,
                    Collections.emptyList());
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

            MemberId contributorMemberId = new MemberId(CONTRIBUTOR_ID);
            Ledger contributorLedger = Ledger.createNew(contributorMemberId);
            when(ledgerRepository.findByMemberId(contributorMemberId)).thenReturn(contributorLedger);

            MemberId parentMemberId = new MemberId(PARENT_ID);
            Ledger parentLedger = Ledger.createNew(parentMemberId);
            when(ledgerRepository.findByMemberId(parentMemberId)).thenReturn(parentLedger);

            MemberId grandparentMemberId = new MemberId(GRANDPARENT_ID);
            Ledger grandparentLedger = Ledger.createNew(grandparentMemberId);
            when(ledgerRepository.findByMemberId(grandparentMemberId)).thenReturn(grandparentLedger);

            // When
            invokeHandle(contribution);

            // Then - verify 3 ledgers saved (contributor + 2 ancestors)
            verify(ledgerRepository, times(3)).save(any(Ledger.class));
            verify(logger).cascadingToAncestors(eq(CONTRIBUTOR_ID), anyList(), eq(contributionAmount));
            verify(logger, times(2)).ancestorLedgerUpdated(any(UUID.class), eq(contributionAmount));
        }

        @Test
        @DisplayName("Should log success when event processed successfully")
        void shouldLogSuccessOnCompletion() {
            // Given
            ContributionMadeDTO contribution = createContributionMadeDTO(100);

            Member contributorMember = new Member(
                    new MemberId(CONTRIBUTOR_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

            MemberId memberId = new MemberId(CONTRIBUTOR_ID);
            Ledger contributorLedger = Ledger.createNew(memberId);
            when(ledgerRepository.findByMemberId(memberId)).thenReturn(contributorLedger);

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).processingEvent(contribution);
            verify(logger).eventProcessedSuccessfully(CONTRIBUTION_ID, CONTRIBUTOR_ID);
        }
    }

    @Nested
    @DisplayName("When member or ledger not found")
    class ErrorHandling {

        @Test
        @DisplayName("Should log error and return when member not found")
        void shouldHandleMissingMember() {
            // Given
            ContributionMadeDTO contribution = createContributionMadeDTO(100);
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(null);

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).eventProcessingFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID),
                    any(IllegalStateException.class));
            verify(ledgerRepository, never()).save(any(Ledger.class));
        }

        @Test
        @DisplayName("Should log error and return when ledger not found")
        void shouldHandleMissingLedger() {
            // Given
            ContributionMadeDTO contribution = createContributionMadeDTO(100);

            Member contributorMember = new Member(
                    new MemberId(CONTRIBUTOR_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);
            when(ledgerRepository.findByMemberId(any(MemberId.class))).thenReturn(null);

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).eventProcessingFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID),
                    any(IllegalStateException.class));
            verify(ledgerRepository, never()).save(any(Ledger.class));
        }

        @Test
        @DisplayName("Should continue processing when ancestor ledger not found")
        void shouldContinueWhenAncestorLedgerNotFound() {
            // Given
            ContributionMadeDTO contribution = createContributionMadeDTO(100);

            List<MemberId> ancestors = List.of(new MemberId(PARENT_ID));
            Member contributorMember = new Member(
                    new MemberId(CONTRIBUTOR_ID),
                    new MemberId(PARENT_ID),
                    ancestors,
                    Collections.emptyList());
            when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

            MemberId contributorMemberId = new MemberId(CONTRIBUTOR_ID);
            Ledger contributorLedger = Ledger.createNew(contributorMemberId);
            when(ledgerRepository.findByMemberId(contributorMemberId)).thenReturn(contributorLedger);

            // Parent ledger not found
            when(ledgerRepository.findByMemberId(new MemberId(PARENT_ID))).thenReturn(null);

            // When
            invokeHandle(contribution);

            // Then - should still save contributor's ledger
            verify(ledgerRepository, times(1)).save(any(Ledger.class));
            verify(logger).eventProcessedSuccessfully(CONTRIBUTION_ID, CONTRIBUTOR_ID);
        }
    }

    private ContributionMadeDTO createContributionMadeDTO(int amount) {
        return new ContributionMadeDTO(
                CONTRIBUTION_ID,
                CONTRIBUTOR_ID,
                12345,
                amount);
    }

    /**
     * Uses reflection to invoke the private handle method for testing
     */
    private void invokeHandle(ContributionMadeDTO contribution) {
        try {
            var handleMethod = ContributionMadeHandler.class.getDeclaredMethod("handle", ContributionMadeDTO.class);
            handleMethod.setAccessible(true);
            handleMethod.invoke(handler, contribution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handle method", e);
        }
    }
}
