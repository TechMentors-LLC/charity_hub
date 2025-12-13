package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionConfirmedLogger;
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
@DisplayName("ContributionConfirmedHandler Tests")
class ContributionConfirmedHandlerTest {

        @Mock
        private IEventBus eventBus;

        @Mock
        private ILedgerRepository ledgerRepository;

        @Mock
        private IMembersNetworkRepo membersNetworkRepo;

        @Mock
        private INotificationService notificationService;

        @Mock
        private ContributionConfirmedLogger logger;

        private ContributionConfirmedHandler handler;

        private final UUID CONTRIBUTOR_ID = UUID.randomUUID();
        private final UUID CONTRIBUTION_ID = UUID.randomUUID();
        private final UUID PARENT_ID = UUID.randomUUID();
        private final UUID GRANDPARENT_ID = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                handler = new ContributionConfirmedHandler(eventBus, ledgerRepository, membersNetworkRepo,
                                notificationService,
                                logger);
        }

        @Nested
        @DisplayName("When handler starts")
        class HandlerRegistration {

                @Test
                @DisplayName("Should register handler and subscribe to ContributionConfirmedDTO events")
                void shouldRegisterHandler() {
                        handler.start();

                        verify(logger).handlerRegistered();
                        verify(eventBus).subscribe(eq(handler), eq(ContributionConfirmedDTO.class), any());
                }
        }

        @Nested
        @DisplayName("When processing contribution confirmed event")
        class ProcessingContributionConfirmed {

                @Test
                @DisplayName("Should debit contributor's dueAmount and dueNetworkAmount")
                void shouldDebitContributorLedgerAmounts() {
                        // Given
                        int contributionAmount = 100;
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(contributionAmount);

                        Member contributorMember = new Member(
                                        new MemberId(CONTRIBUTOR_ID),
                                        null,
                                        Collections.emptyList(),
                                        Collections.emptyList());
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

                        // Create ledger with existing balances to verify debit
                        MemberId memberId = new MemberId(CONTRIBUTOR_ID);
                        Ledger contributorLedger = new Ledger(
                                        LedgerId.fromMemberId(memberId),
                                        memberId,
                                        Amount.forMember(200),
                                        Amount.forNetwork(200),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(memberId)).thenReturn(contributorLedger);

                        // When
                        invokeHandle(contribution);

                        // Then
                        ArgumentCaptor<Ledger> ledgerCaptor = ArgumentCaptor.forClass(Ledger.class);
                        verify(ledgerRepository).save(ledgerCaptor.capture());

                        Ledger savedLedger = ledgerCaptor.getValue();
                        assertThat(savedLedger.getDueAmount().value()).isEqualTo(100); // 200 - 100
                        assertThat(savedLedger.getDueNetworkAmount().value()).isEqualTo(100); // 200 - 100
                }

                @Test
                @DisplayName("Should cascade debit dueNetworkAmount to all ancestors")
                void shouldCascadeDebitToAncestors() {
                        // Given
                        int contributionAmount = 100;
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(contributionAmount);

                        List<MemberId> ancestors = List.of(new MemberId(PARENT_ID), new MemberId(GRANDPARENT_ID));
                        Member contributorMember = new Member(
                                        new MemberId(CONTRIBUTOR_ID),
                                        new MemberId(PARENT_ID),
                                        ancestors,
                                        Collections.emptyList());
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

                        MemberId contributorMemberId = new MemberId(CONTRIBUTOR_ID);
                        Ledger contributorLedger = new Ledger(
                                        LedgerId.fromMemberId(contributorMemberId),
                                        contributorMemberId,
                                        Amount.forMember(200),
                                        Amount.forNetwork(200),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(contributorMemberId)).thenReturn(contributorLedger);

                        MemberId parentMemberId = new MemberId(PARENT_ID);
                        Ledger parentLedger = new Ledger(
                                        LedgerId.fromMemberId(parentMemberId),
                                        parentMemberId,
                                        Amount.forMember(0),
                                        Amount.forNetwork(500),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(parentMemberId)).thenReturn(parentLedger);

                        MemberId grandparentMemberId = new MemberId(GRANDPARENT_ID);
                        Ledger grandparentLedger = new Ledger(
                                        LedgerId.fromMemberId(grandparentMemberId),
                                        grandparentMemberId,
                                        Amount.forMember(0),
                                        Amount.forNetwork(500),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(grandparentMemberId)).thenReturn(grandparentLedger);

                        // When
                        invokeHandle(contribution);

                        // Then - verify cascading happened
                        verify(logger).cascadingToAncestors(eq(CONTRIBUTOR_ID), anyList(), eq(contributionAmount));
                        verify(logger, times(2)).ancestorLedgerUpdated(any(UUID.class), eq(-contributionAmount));
                }

                @Test
                @DisplayName("Should credit parent's dueAmount when parent exists")
                void shouldCreditParentDueAmount() {
                        // Given
                        int contributionAmount = 100;
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(contributionAmount);

                        Member contributorMember = new Member(
                                        new MemberId(CONTRIBUTOR_ID),
                                        new MemberId(PARENT_ID),
                                        Collections.emptyList(),
                                        Collections.emptyList());
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

                        MemberId contributorMemberId = new MemberId(CONTRIBUTOR_ID);
                        Ledger contributorLedger = new Ledger(
                                        LedgerId.fromMemberId(contributorMemberId),
                                        contributorMemberId,
                                        Amount.forMember(200),
                                        Amount.forNetwork(200),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(contributorMemberId)).thenReturn(contributorLedger);

                        MemberId parentMemberId = new MemberId(PARENT_ID);
                        Ledger parentLedger = Ledger.createNew(parentMemberId);
                        when(ledgerRepository.findByMemberId(parentMemberId)).thenReturn(parentLedger);

                        // When
                        invokeHandle(contribution);

                        // Then
                        verify(logger).parentObligationCreated(PARENT_ID, contributionAmount);
                }

                @Test
                @DisplayName("Should send notification on successful processing")
                void shouldSendNotificationOnSuccess() {
                        // Given
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(100);

                        Member contributorMember = new Member(
                                        new MemberId(CONTRIBUTOR_ID),
                                        null,
                                        Collections.emptyList(),
                                        Collections.emptyList());
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

                        MemberId memberId = new MemberId(CONTRIBUTOR_ID);
                        Ledger contributorLedger = new Ledger(
                                        LedgerId.fromMemberId(memberId),
                                        memberId,
                                        Amount.forMember(200),
                                        Amount.forNetwork(200),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(memberId)).thenReturn(contributorLedger);

                        // When
                        invokeHandle(contribution);

                        // Then
                        verify(notificationService).notifyContributionConfirmed(contribution);
                        verify(logger).notificationSent(CONTRIBUTION_ID, CONTRIBUTOR_ID);
                }
        }

        @Nested
        @DisplayName("When member or ledger not found")
        class ErrorHandling {

                @Test
                @DisplayName("Should log error and return when member not found")
                void shouldHandleMissingMember() {
                        // Given
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(100);
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(null);

                        // When
                        invokeHandle(contribution);

                        // Then
                        verify(logger).eventProcessingFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID),
                                        any(IllegalStateException.class));
                        verify(ledgerRepository, never()).save(any(Ledger.class));
                        verify(notificationService, never()).notifyContributionConfirmed(any());
                }

                @Test
                @DisplayName("Should log error and return when ledger not found")
                void shouldHandleMissingLedger() {
                        // Given
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(100);

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
                @DisplayName("Should handle notification failure gracefully")
                void shouldHandleNotificationFailure() {
                        // Given
                        ContributionConfirmedDTO contribution = createContributionConfirmedDTO(100);

                        Member contributorMember = new Member(
                                        new MemberId(CONTRIBUTOR_ID),
                                        null,
                                        Collections.emptyList(),
                                        Collections.emptyList());
                        when(membersNetworkRepo.getById(CONTRIBUTOR_ID)).thenReturn(contributorMember);

                        MemberId memberId = new MemberId(CONTRIBUTOR_ID);
                        Ledger contributorLedger = new Ledger(
                                        LedgerId.fromMemberId(memberId),
                                        memberId,
                                        Amount.forMember(200),
                                        Amount.forNetwork(200),
                                        Collections.emptyList());
                        when(ledgerRepository.findByMemberId(memberId)).thenReturn(contributorLedger);

                        doThrow(new RuntimeException("Notification failed")).when(notificationService)
                                        .notifyContributionConfirmed(any());

                        // When
                        invokeHandle(contribution);

                        // Then - should catch exception and log failure
                        verify(logger).notificationFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID),
                                        any(Exception.class));
                }
        }

        private ContributionConfirmedDTO createContributionConfirmedDTO(int amount) {
                return new ContributionConfirmedDTO(
                                CONTRIBUTION_ID,
                                CONTRIBUTOR_ID,
                                amount);
        }

        /**
         * Uses reflection to invoke the private handle method for testing
         */
        private void invokeHandle(ContributionConfirmedDTO contribution) {
                try {
                        var handleMethod = ContributionConfirmedHandler.class.getDeclaredMethod("handle",
                                        ContributionConfirmedDTO.class);
                        handleMethod.setAccessible(true);
                        handleMethod.invoke(handler, contribution);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to invoke handle method", e);
                }
        }
}
