package com.charity_hub.cases.internal.infrastructure.queryhandlers;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseQuery;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseResponse;
import com.charity_hub.cases.internal.infrastructure.db.CaseEntity;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import com.charity_hub.cases.internal.infrastructure.gateways.AccountsGateway;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCaseHandler Tests")
class GetCaseHandlerTest {

    @Mock
    private ICaseReadRepo caseRepo;

    @Mock
    private GetCaseMapper getCaseMapper;

    @Mock
    private AccountsGateway accountsGateway;

    private GetCaseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetCaseHandler(caseRepo, getCaseMapper, accountsGateway);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should return case details for full access user with contributors")
        void shouldReturnCaseDetailsWithContributorsForFullAccessUser() {
            // Arrange
            int caseCode = 12345;
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId, List.of("FULL_ACCESS"));
            var query = new GetCaseQuery(caseCode, accessTokenPayload);

            var caseEntity = createCaseEntity(caseCode);
            var contribution = createContributionEntity(caseCode, userId.toString());
            var accountDto = new AccountDTO(userId.toString(), "+1234567890", "John Doe", "http://photo.url", List.of());
            var caseDetails = createCaseDetails(caseCode, List.of());

            when(caseRepo.getByCode(caseCode)).thenReturn(caseEntity);
            when(caseRepo.getContributionsByCaseCode(caseCode)).thenReturn(List.of(contribution));
            when(accountsGateway.getAccountsByIds(anyList())).thenReturn(List.of(accountDto));
            when(getCaseMapper.toCaseDetails(any(), anyList(), anyList())).thenReturn(caseDetails);

            // Act
            GetCaseResponse result = handler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getCase().code()).isEqualTo(caseCode);
            verify(accountsGateway).getAccountsByIds(anyList());
        }

        @Test
        @DisplayName("should return case details without contributors for non-full access user")
        void shouldReturnCaseDetailsWithoutContributorsForNonFullAccessUser() {
            // Arrange
            int caseCode = 12345;
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId, List.of("CONTRIBUTE"));
            var query = new GetCaseQuery(caseCode, accessTokenPayload);

            var caseEntity = createCaseEntity(caseCode);
            var contribution = createContributionEntity(caseCode, userId.toString());
            var caseDetails = createCaseDetails(caseCode, List.of());

            when(caseRepo.getByCode(caseCode)).thenReturn(caseEntity);
            when(caseRepo.getContributionsByCaseCode(caseCode)).thenReturn(List.of(contribution));
            when(getCaseMapper.toCaseDetails(any(), anyList(), any())).thenReturn(caseDetails);

            // Act
            GetCaseResponse result = handler.handle(query);

            // Assert
            assertThat(result).isNotNull();
            verify(accountsGateway, never()).getAccountsByIds(anyList());
        }

        @Test
        @DisplayName("should throw NotFoundException when case not found")
        void shouldThrowNotFoundExceptionWhenCaseNotFound() {
            // Arrange
            int caseCode = 99999;
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId, List.of("FULL_ACCESS"));
            var query = new GetCaseQuery(caseCode, accessTokenPayload);

            when(caseRepo.getByCode(caseCode)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(query))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("99999")
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("should retrieve contributions for the case")
        void shouldRetrieveContributionsForCase() {
            // Arrange
            int caseCode = 12345;
            UUID userId = UUID.randomUUID();
            var accessTokenPayload = createAccessTokenPayload(userId, List.of("CONTRIBUTE"));
            var query = new GetCaseQuery(caseCode, accessTokenPayload);

            var caseEntity = createCaseEntity(caseCode);
            var caseDetails = createCaseDetails(caseCode, List.of());

            when(caseRepo.getByCode(caseCode)).thenReturn(caseEntity);
            when(caseRepo.getContributionsByCaseCode(caseCode)).thenReturn(List.of());
            when(getCaseMapper.toCaseDetails(any(), anyList(), any())).thenReturn(caseDetails);

            // Act
            handler.handle(query);

            // Assert
            verify(caseRepo).getContributionsByCaseCode(caseCode);
        }

        private AccessTokenPayload createAccessTokenPayload(UUID userId, List<String> permissions) {
            return new AccessTokenPayload(
                    "test-audience",
                    "test-jwt-id",
                    new Date(System.currentTimeMillis() + 3600000),
                    new Date(),
                    userId.toString(),
                    "Test User",
                    "http://photo.url",
                    false,
                    "+1234567890",
                    "test-device-id",
                    permissions
            );
        }

        private CaseEntity createCaseEntity(int code) {
            return new CaseEntity(
                    code,
                    "Test Case",
                    "Test Description",
                    10000,
                    5000,
                    CaseEntity.STATUS_OPENED,
                    true,
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    List.of(),
                    List.of(),
                    0
            );
        }

        private ContributionEntity createContributionEntity(int caseCode, String contributorId) {
            return new ContributionEntity(
                    "contrib-123",
                    contributorId,
                    caseCode,
                    1000,
                    2,
                    System.currentTimeMillis(),
                    null
            );
        }

        private GetCaseResponse.CaseDetails createCaseDetails(int code, List<GetCaseResponse.Contribution> contributions) {
            return new GetCaseResponse.CaseDetails(
                    code,
                    "Test Case",
                    "Test Description",
                    10000,
                    5000,
                    true,
                    "OPEN",
                    System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    contributions,
                    List.of()
            );
        }
    }
}
