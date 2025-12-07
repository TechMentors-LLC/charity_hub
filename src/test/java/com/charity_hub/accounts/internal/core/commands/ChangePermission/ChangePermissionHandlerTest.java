package com.charity_hub.accounts.internal.core.commands.ChangePermission;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangePermissionHandler Tests")
class ChangePermissionHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @InjectMocks
    private ChangePermissionHandler handler;

    private final UUID USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("Should add permission to existing account")
    void shouldAddPermissionToExistingAccount() {
        Account account = mock(Account.class);
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.of(account));

        ChangePermission command = new ChangePermission(USER_ID, "EDIT", true);
        handler.handle(command);

        verify(account).addPermission("EDIT");
        verify(account, never()).removePermission(anyString());
        verify(accountRepo).save(account);
    }

    @Test
    @DisplayName("Should remove permission from existing account")
    void shouldRemovePermissionFromExistingAccount() {
        Account account = mock(Account.class);
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.of(account));

        ChangePermission command = new ChangePermission(USER_ID, "EDIT", false);
        handler.handle(command);

        verify(account).removePermission("EDIT");
        verify(account, never()).addPermission(anyString());
        verify(accountRepo).save(account);
    }

    @Test
    @DisplayName("Should throw NotFoundException when account not found")
    void shouldThrowNotFoundWhenAccountNotFound() {
        when(accountRepo.getById(USER_ID)).thenReturn(Optional.empty());

        ChangePermission command = new ChangePermission(USER_ID, "EDIT", true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(USER_ID.toString());
    }
}
