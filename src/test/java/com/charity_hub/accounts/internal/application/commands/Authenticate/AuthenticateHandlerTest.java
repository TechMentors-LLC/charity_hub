package com.charity_hub.accounts.internal.application.commands.Authenticate;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.domain.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.application.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.domain.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.shared.domain.model.Pair;
import com.charity_hub.shared.exceptions.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateHandler Tests")
class AuthenticateHandlerTest {

    @Mock
    private IAccountRepo accountRepo;

    @Mock
    private IInvitationRepo invitationRepo;

    @Mock
    private IAuthProvider authProvider;

    @Mock
    private IJWTGenerator jwtGenerator;

    @InjectMocks
    private AuthenticateHandler handler;

    private static final String VALID_ID_TOKEN = "valid-id-token";
    private static final String DEVICE_ID = "device-123456789012345";  // 21 characters
    private static final String DEVICE_TYPE = "ANDROID";
    private static final String MOBILE_NUMBER = "1234567890";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Nested
    @DisplayName("When authenticating existing account")
    class ExistingAccount {

        @Mock
        private Account existingAccount;

        @BeforeEach
        void setUp() {
            when(authProvider.getVerifiedMobileNumber(VALID_ID_TOKEN)).thenReturn(MOBILE_NUMBER);
            when(accountRepo.getByMobileNumber(MOBILE_NUMBER)).thenReturn(Optional.of(existingAccount));
            when(existingAccount.authenticate(anyString(), anyString(), any(IJWTGenerator.class)))
                    .thenReturn(new Pair<>(ACCESS_TOKEN, REFRESH_TOKEN));
        }

        @Test
        @DisplayName("Should return tokens for existing account")
        void shouldReturnTokensForExistingAccount() {
            Authenticate command = new Authenticate(VALID_ID_TOKEN, DEVICE_ID, DEVICE_TYPE);

            AuthenticateResponse response = handler.handle(command);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
            verify(accountRepo).save(existingAccount);
        }

        @Test
        @DisplayName("Should not check invitations for existing account")
        void shouldNotCheckInvitationsForExistingAccount() {
            Authenticate command = new Authenticate(VALID_ID_TOKEN, DEVICE_ID, DEVICE_TYPE);

            handler.handle(command);

            verifyNoInteractions(invitationRepo);
        }
    }

    @Nested
    @DisplayName("When authenticating new account")
    class NewAccount {

        @BeforeEach
        void setUp() {
            when(authProvider.getVerifiedMobileNumber(VALID_ID_TOKEN)).thenReturn(MOBILE_NUMBER);
            when(accountRepo.getByMobileNumber(MOBILE_NUMBER)).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("Should create new account for admin")
        void shouldCreateNewAccountForAdmin() {
            when(accountRepo.isAdmin(MOBILE_NUMBER)).thenReturn(true);
            when(jwtGenerator.generateAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);
            when(jwtGenerator.generateRefreshToken(any(), any())).thenReturn(REFRESH_TOKEN);

            Authenticate command = new Authenticate(VALID_ID_TOKEN, DEVICE_ID, DEVICE_TYPE);
            AuthenticateResponse response = handler.handle(command);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);

            ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepo).save(accountCaptor.capture());
            assertThat(accountCaptor.getValue().getMobileNumber().value()).isEqualTo(MOBILE_NUMBER);
        }

        @Test
        @DisplayName("Should create new account for invited user")
        void shouldCreateNewAccountForInvitedUser() {
            when(accountRepo.isAdmin(MOBILE_NUMBER)).thenReturn(false);
            when(invitationRepo.hasInvitation(MOBILE_NUMBER)).thenReturn(true);
            when(jwtGenerator.generateAccessToken(any(), any())).thenReturn(ACCESS_TOKEN);
            when(jwtGenerator.generateRefreshToken(any(), any())).thenReturn(REFRESH_TOKEN);

            Authenticate command = new Authenticate(VALID_ID_TOKEN, DEVICE_ID, DEVICE_TYPE);
            AuthenticateResponse response = handler.handle(command);

            assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
            verify(accountRepo).save(any(Account.class));
        }

        @Test
        @DisplayName("Should reject non-invited non-admin user")
        void shouldRejectNonInvitedNonAdminUser() {
            when(accountRepo.isAdmin(MOBILE_NUMBER)).thenReturn(false);
            when(invitationRepo.hasInvitation(MOBILE_NUMBER)).thenReturn(false);

            Authenticate command = new Authenticate(VALID_ID_TOKEN, DEVICE_ID, DEVICE_TYPE);

            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Account not invited");

            verify(accountRepo, never()).save(any());
        }
    }
}
