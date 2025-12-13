package com.charity_hub.accounts.internal.domain.model.account;

import com.charity_hub.accounts.internal.domain.contracts.IJWTGenerator;
import com.charity_hub.shared.domain.model.Pair;
import com.charity_hub.shared.domain.model.Permission;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Domain Model Tests")
class AccountTest {

    @Mock
    private IJWTGenerator jwtGenerator;

    private static final String MOBILE_NUMBER = "1234567890";
    private static final String DEVICE_ID = "device-123456789012345";
    private static final String DEVICE_TYPE = "ANDROID";

    @Nested
    @DisplayName("When creating new account")
    class NewAccountCreation {

        @Test
        @DisplayName("Should create account with correct mobile number")
        void shouldCreateAccountWithCorrectMobileNumber() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);

            assertThat(account.getMobileNumber().value()).isEqualTo(MOBILE_NUMBER);
        }

        @Test
        @DisplayName("Should create admin account with FULL_ACCESS permission")
        void shouldCreateAdminAccountWithFullAccessPermission() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, true);

            assertThat(account.getPermissions()).contains(Permission.FULL_ACCESS);
        }

        @Test
        @DisplayName("Should create regular account with VIEW permission")
        void shouldCreateRegularAccountWithViewPermission() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);

            assertThat(account.getPermissions()).contains(Permission.VIEW);
        }

        @Test
        @DisplayName("Should create account with one device")
        void shouldCreateAccountWithOneDevice() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);

            assertThat(account.getDevices()).hasSize(1);
        }

        @Test
        @DisplayName("Should create account not blocked")
        void shouldCreateAccountNotBlocked() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);

            assertThat(account.isBlocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("When managing permissions")
    class PermissionManagement {

        private Account account;

        @BeforeEach
        void setUp() {
            account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);
        }

        @Test
        @DisplayName("Should add permission to account")
        void shouldAddPermissionToAccount() {
            account.addPermission("CREATE_CASES");

            assertThat(account.getPermissions()).contains(Permission.CREATE_CASES);
        }

        @Test
        @DisplayName("Should remove permission from account")
        void shouldRemovePermissionFromAccount() {
            account.addPermission("CREATE_CASES");
            account.removePermission("CREATE_CASES");

            assertThat(account.getPermissions()).doesNotContain(Permission.CREATE_CASES);
        }
    }

    @Nested
    @DisplayName("When blocking/unblocking account")
    class BlockingAccount {

        private Account account;

        @BeforeEach
        void setUp() {
            account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);
        }

        @Test
        @DisplayName("Should block account")
        void shouldBlockAccount() {
            account.block();

            assertThat(account.isBlocked()).isTrue();
        }

        @Test
        @DisplayName("Should unblock account")
        void shouldUnblockAccount() {
            account.block();
            account.unBlock();

            assertThat(account.isBlocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("When authenticating")
    class Authentication {

        @Test
        @DisplayName("Should return tokens on authentication")
        void shouldReturnTokensOnAuthentication() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);
            when(jwtGenerator.generateAccessToken(any(), any())).thenReturn("access-token");
            when(jwtGenerator.generateRefreshToken(any(), any())).thenReturn("refresh-token");

            Pair<String, String> tokens = account.authenticate(DEVICE_ID, DEVICE_TYPE, jwtGenerator);

            assertThat(tokens.first).isEqualTo("access-token");
            assertThat(tokens.second).isEqualTo("refresh-token");
        }
    }

    @Nested
    @DisplayName("When updating basic info")
    class UpdateBasicInfo {

        @Test
        @DisplayName("Should update full name and photo URL")
        void shouldUpdateFullNameAndPhotoUrl() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);
            when(jwtGenerator.generateAccessToken(any(), any())).thenReturn("new-access-token");

            String newToken = account.updateBasicInfo(DEVICE_ID, "John Doe", "https://photo.url", jwtGenerator);

            assertThat(account.getFullName().value()).isEqualTo("John Doe");
            assertThat(account.getPhotoUrl().value()).isEqualTo("https://photo.url");
            assertThat(newToken).isEqualTo("new-access-token");
        }

        @Test
        @DisplayName("Should throw NotFoundException for unknown device")
        void shouldThrowNotFoundForUnknownDevice() {
            Account account = Account.newAccount(MOBILE_NUMBER, DEVICE_ID, DEVICE_TYPE, false);

            assertThatThrownBy(() -> account.updateBasicInfo("unknown-device-id-123456", "John", "url", jwtGenerator))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Device not found");
        }
    }
}
