package com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated;

import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.models.InvitationResponse;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.AccountCreatedEventLogger;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.shared.domain.ILogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountCreatedEventHandler Tests")
class AccountCreatedEventHandlerTest {

    @Mock
    private IMembersNetworkRepo membersNetworkRepo;

    @Mock
    private ILedgerRepository ledgerRepository;

    @Mock
    private IAccountGateway invitationGateway;

    @Mock
    private INotificationService notificationService;

    @Mock
    private AccountCreatedEventLogger logger;

    @Mock
    private ILogger rawLogger;

    private AccountCreatedEventHandler handler;

    private final UUID ACCOUNT_ID = UUID.randomUUID();
    private final UUID PARENT_ID = UUID.randomUUID();
    private final String MOBILE_NUMBER = "+201234567890";

    @BeforeEach
    void setUp() {
        handler = new AccountCreatedEventHandler(
                membersNetworkRepo,
                ledgerRepository,
                invitationGateway,
                notificationService,
                logger,
                rawLogger);
    }

    @Nested
    @DisplayName("When no invitation exists (root user)")
    class RootUserCreation {

        @Test
        @DisplayName("Should create root member when no invitation found")
        void shouldCreateRootMemberWhenNoInvitation() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(null);

            // When
            handler.accountCreatedHandler(account);

            // Then
            verify(logger).invitationNotFound(ACCOUNT_ID, MOBILE_NUMBER);

            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(membersNetworkRepo).save(memberCaptor.capture());

            Member savedMember = memberCaptor.getValue();
            assertThat(savedMember.memberId().value()).isEqualTo(ACCOUNT_ID);
            assertThat(savedMember.parent()).isNull();
            assertThat(savedMember.ancestors()).isEmpty();
        }

        @Test
        @DisplayName("Should create ledger for root member")
        void shouldCreateLedgerForRootMember() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(null);

            // When
            handler.accountCreatedHandler(account);

            // Then
            ArgumentCaptor<Ledger> ledgerCaptor = ArgumentCaptor.forClass(Ledger.class);
            verify(ledgerRepository).save(ledgerCaptor.capture());

            Ledger savedLedger = ledgerCaptor.getValue();
            assertThat(savedLedger.getMemberId().value()).isEqualTo(ACCOUNT_ID);
            assertThat(savedLedger.getDueAmount().value()).isZero();
            assertThat(savedLedger.getDueNetworkAmount().value()).isZero();
        }
    }

    @Nested
    @DisplayName("When invitation exists (invited user)")
    class InvitedUserCreation {

        @Test
        @DisplayName("Should create member with parent when invitation found")
        void shouldCreateMemberWithParent() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);

            Member parentMember = new Member(
                    new MemberId(PARENT_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(parentMember);

            // When
            handler.accountCreatedHandler(account);

            // Then
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(membersNetworkRepo, times(2)).save(memberCaptor.capture());

            // First save is new member, second is updated parent
            Member newMember = memberCaptor.getAllValues().get(0);
            assertThat(newMember.memberId().value()).isEqualTo(ACCOUNT_ID);
            assertThat(newMember.parent().value()).isEqualTo(PARENT_ID);
        }

        @Test
        @DisplayName("Should update parent with new child")
        void shouldUpdateParentWithNewChild() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);

            Member parentMember = new Member(
                    new MemberId(PARENT_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(parentMember);

            // When
            handler.accountCreatedHandler(account);

            // Then
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(membersNetworkRepo, times(2)).save(memberCaptor.capture());

            // Second save is updated parent
            Member updatedParent = memberCaptor.getAllValues().get(1);
            assertThat(updatedParent.memberId().value()).isEqualTo(PARENT_ID);
            assertThat(updatedParent.children()).hasSize(1);
            assertThat(updatedParent.children().get(0).value()).isEqualTo(ACCOUNT_ID);
        }

        @Test
        @DisplayName("Should create ledger for invited member")
        void shouldCreateLedgerForInvitedMember() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);

            Member parentMember = new Member(
                    new MemberId(PARENT_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(parentMember);

            // When
            handler.accountCreatedHandler(account);

            // Then
            ArgumentCaptor<Ledger> ledgerCaptor = ArgumentCaptor.forClass(Ledger.class);
            verify(ledgerRepository).save(ledgerCaptor.capture());

            Ledger savedLedger = ledgerCaptor.getValue();
            assertThat(savedLedger.getMemberId().value()).isEqualTo(ACCOUNT_ID);
        }

        @Test
        @DisplayName("Should send notification for new connection")
        void shouldSendNotificationForNewConnection() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);

            Member parentMember = new Member(
                    new MemberId(PARENT_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(parentMember);

            // When
            handler.accountCreatedHandler(account);

            // Then
            verify(notificationService).notifyNewConnectionAdded(any(Member.class));
            verify(logger).membershipCreated(ACCOUNT_ID, PARENT_ID);
        }
    }

    @Nested
    @DisplayName("When errors occur")
    class ErrorHandling {

        @Test
        @DisplayName("Should log error when parent member not found")
        void shouldLogErrorWhenParentNotFound() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(null);

            // When
            handler.accountCreatedHandler(account);

            // Then
            verify(logger).parentMemberNotFound(ACCOUNT_ID, PARENT_ID);
            verify(membersNetworkRepo, never()).save(any(Member.class));
            verify(ledgerRepository, never()).save(any(Ledger.class));
        }

        @Test
        @DisplayName("Should handle membership creation failure gracefully")
        void shouldHandleMembershipCreationFailure() {
            // Given
            AccountCreated account = new AccountCreated(ACCOUNT_ID, MOBILE_NUMBER);
            InvitationResponse invitation = new InvitationResponse(MOBILE_NUMBER, PARENT_ID);
            when(invitationGateway.getInvitationByMobileNumber(MOBILE_NUMBER)).thenReturn(invitation);

            Member parentMember = new Member(
                    new MemberId(PARENT_ID),
                    null,
                    Collections.emptyList(),
                    Collections.emptyList());
            when(membersNetworkRepo.getById(PARENT_ID)).thenReturn(parentMember);

            doThrow(new RuntimeException("Database error")).when(membersNetworkRepo).save(any(Member.class));

            // When
            handler.accountCreatedHandler(account);

            // Then
            verify(logger).membershipCreationFailed(eq(ACCOUNT_ID), eq(PARENT_ID), any(Exception.class));
        }
    }
}
