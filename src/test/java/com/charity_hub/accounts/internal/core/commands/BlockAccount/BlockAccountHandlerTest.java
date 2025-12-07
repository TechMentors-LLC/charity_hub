package com.charity_hub.accounts.internal.core.commands.BlockAccount;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("BlockAccountHandler Tests")
class BlockAccountHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @InjectMocks
    private BlockAccountHandler handler;

    private final String USER_ID = UUID.randomUUID().toString();

    @Nested
    @DisplayName("When blocking account")
    class BlockingAccount {

        @Test
        @DisplayName("Should block existing account")
        void shouldBlockExistingAccount() {
            Account account = mock(Account.class);
            when(accountRepo.getById(UUID.fromString(USER_ID))).thenReturn(Optional.of(account));

            BlockAccount command = new BlockAccount(USER_ID, false);
            handler.handle(command);

            verify(account).block();
            verify(account, never()).unBlock();
            verify(accountRepo).save(account);
        }

        @Test
        @DisplayName("Should throw NotFoundException when account not found")
        void shouldThrowNotFoundWhenAccountNotFound() {
            when(accountRepo.getById(UUID.fromString(USER_ID))).thenReturn(Optional.empty());

            BlockAccount command = new BlockAccount(USER_ID, false);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(USER_ID);
        }
    }

    @Nested
    @DisplayName("When unblocking account")
    class UnblockingAccount {

        @Test
        @DisplayName("Should unblock existing account")
        void shouldUnblockExistingAccount() {
            Account account = mock(Account.class);
            when(accountRepo.getById(UUID.fromString(USER_ID))).thenReturn(Optional.of(account));

            BlockAccount command = new BlockAccount(USER_ID, true);
            handler.handle(command);

            verify(account).unBlock();
            verify(account, never()).block();
            verify(accountRepo).save(account);
        }
    }
}
