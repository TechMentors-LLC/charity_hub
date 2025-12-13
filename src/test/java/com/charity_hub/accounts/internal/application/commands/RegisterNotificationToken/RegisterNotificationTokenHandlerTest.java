package com.charity_hub.accounts.internal.application.commands.RegisterNotificationToken;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterNotificationTokenHandler Tests")
class RegisterNotificationTokenHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @InjectMocks
    private RegisterNotificationTokenHandler handler;

    private final UUID USER_ID = UUID.randomUUID();
    private final String DEVICE_ID = "device-123456789012345";
    private final String FCM_TOKEN = "fcm-token-123";

    @Test
    @DisplayName("Should register FCM token for existing account")
    void shouldRegisterFcmTokenForExistingAccount() {
        Account account = mock(Account.class);
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.of(account));

        RegisterNotificationToken command = new RegisterNotificationToken(FCM_TOKEN, DEVICE_ID, USER_ID);
        handler.handle(command);

        verify(account).registerFCMToken(DEVICE_ID, FCM_TOKEN);
        verify(accountRepo).save(account);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account not found")
    void shouldThrowNotFoundWhenAccountNotFound() {
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.empty());

        RegisterNotificationToken command = new RegisterNotificationToken(FCM_TOKEN, DEVICE_ID, USER_ID);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(USER_ID.toString());
    }
}
