package com.charity_hub.accounts.internal.core.commands.InviteAccount;

import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.exceptions.AlreadyInvitedException;
import com.charity_hub.accounts.internal.core.model.invitation.Invitation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InviteAccountHandler Tests")
class InviteAccountHandlerTest {

    @Mock
    private IInvitationRepo invitationRepo;

    @InjectMocks
    private InviteAccountHandler handler;

    private static final String MOBILE_NUMBER = "1234567890";
    private static final UUID INVITER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should create invitation for new mobile number")
    void shouldCreateInvitationForNewMobileNumber() {
        when(invitationRepo.hasInvitation(MOBILE_NUMBER)).thenReturn(false);

        InvitationAccount command = new InvitationAccount(MOBILE_NUMBER, INVITER_ID);
        handler.handle(command);

        ArgumentCaptor<Invitation> invitationCaptor = ArgumentCaptor.forClass(Invitation.class);
        verify(invitationRepo).save(invitationCaptor.capture());

        Invitation savedInvitation = invitationCaptor.getValue();
        assertThat(savedInvitation.invitedMobileNumber().value()).isEqualTo(MOBILE_NUMBER);
        assertThat(savedInvitation.inviterId()).isEqualTo(INVITER_ID);
    }

    @Test
    @DisplayName("Should throw AlreadyInvitedException for duplicate invitation")
    void shouldThrowAlreadyInvitedExceptionForDuplicate() {
        when(invitationRepo.hasInvitation(MOBILE_NUMBER)).thenReturn(true);

        InvitationAccount command = new InvitationAccount(MOBILE_NUMBER, INVITER_ID);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(AlreadyInvitedException.class)
                .hasMessageContaining("already invited");

        verify(invitationRepo, never()).save(any());
    }
}
