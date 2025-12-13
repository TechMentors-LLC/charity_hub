package com.charity_hub.accounts.internal.application.commands.RefreshToken;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.domain.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.UnAuthorized;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenHandler Tests")
class RefreshTokenHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @Mock
    private IJWTGenerator jwtGenerator;

    @Mock
    private ILogger logger;

    @InjectMocks
    private RefreshTokenHandler handler;

    private final UUID USER_ID = UUID.randomUUID();
    private final String DEVICE_ID = "device-123456789012345";
    private final String REFRESH_TOKEN = "encoded-refresh-token";
    private final String NEW_ACCESS_TOKEN = "new-access-token";

    @Test
    @DisplayName("Should return new access token for valid refresh token")
    void shouldReturnNewAccessTokenForValidRefreshToken() {
        Account account = mock(Account.class);
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.of(account));
        when(account.refreshAccessToken(DEVICE_ID, REFRESH_TOKEN, jwtGenerator)).thenReturn(NEW_ACCESS_TOKEN);

        RefreshToken command = new RefreshToken(REFRESH_TOKEN, USER_ID, DEVICE_ID);
        String result = handler.handle(command);

        assertThat(result).isEqualTo(NEW_ACCESS_TOKEN);
        verify(accountRepo).save(account);
    }

    @Test
    @DisplayName("Should throw UnAuthorized when account not found")
    void shouldThrowUnAuthorizedWhenAccountNotFound() {
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.empty());

        RefreshToken command = new RefreshToken(REFRESH_TOKEN, USER_ID, DEVICE_ID);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(UnAuthorized.class);
    }
}
