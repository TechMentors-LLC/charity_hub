package com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.core.model.account.Account;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBasicInfoHandler Tests")
class UpdateBasicInfoHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @Mock
    private IJWTGenerator jwtGenerator;

    @InjectMocks
    private UpdateBasicInfoHandler handler;

    private final UUID USER_ID = UUID.randomUUID();
    private final String DEVICE_ID = "device-123456789012345";
    private final String FULL_NAME = "John Doe";
    private final String PHOTO_URL = "https://example.com/photo.jpg";
    private final String NEW_ACCESS_TOKEN = "new-access-token";

    @Test
    @DisplayName("Should update basic info and return new access token")
    void shouldUpdateBasicInfoAndReturnNewAccessToken() {
        Account account = mock(Account.class);
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.of(account));
        when(account.updateBasicInfo(DEVICE_ID, FULL_NAME, PHOTO_URL, jwtGenerator)).thenReturn(NEW_ACCESS_TOKEN);

        UpdateBasicInfo command = new UpdateBasicInfo(USER_ID, DEVICE_ID, FULL_NAME, PHOTO_URL);
        String result = handler.handle(command);

        assertThat(result).isEqualTo(NEW_ACCESS_TOKEN);
        verify(accountRepo).save(account);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account not found")
    void shouldThrowNotFoundWhenAccountNotFound() {
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.empty());

        UpdateBasicInfo command = new UpdateBasicInfo(USER_ID, DEVICE_ID, FULL_NAME, PHOTO_URL);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(USER_ID.toString());
    }
}
