package com.charity_hub.ledger.internal.application.queries.GetLedgerSummary;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.infrastructure.gateways.CasesGateway;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLedgerSummaryHandler Tests")
class GetLedgerSummaryHandlerTest {

    @Mock
    private MembersNetworkRepo membersNetworkRepo;

    @Mock
    private CasesGateway casesGateway;

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private GetLedgerSummaryHandler handler;

    private final UUID USER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("When user has contributions")
    class UserHasContributions {

        @Test
        @DisplayName("Should calculate pledged amount correctly (status=1)")
        void shouldCalculatePledgedAmountCorrectly() {
            List<ContributionDTO> contributions = List.of(
                    createContribution(USER_ID.toString(), 100, 1), // pledged
                    createContribution(USER_ID.toString(), 200, 1), // pledged
                    createContribution(USER_ID.toString(), 50, 2)   // paid (not pledged)
            );
            when(casesGateway.getContributions(USER_ID)).thenReturn(contributions);
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(null);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.pledged()).isEqualTo(300);
        }

        @Test
        @DisplayName("Should calculate paid amount correctly (status=2)")
        void shouldCalculatePaidAmountCorrectly() {
            List<ContributionDTO> contributions = List.of(
                    createContribution(USER_ID.toString(), 100, 2), // paid
                    createContribution(USER_ID.toString(), 250, 2), // paid
                    createContribution(USER_ID.toString(), 50, 1)   // pledged (not paid)
            );
            when(casesGateway.getContributions(USER_ID)).thenReturn(contributions);
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(null);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.paid()).isEqualTo(350);
        }

        @Test
        @DisplayName("Should calculate confirmed amount correctly (status=3)")
        void shouldCalculateConfirmedAmountCorrectly() {
            List<ContributionDTO> contributions = List.of(
                    createContribution(USER_ID.toString(), 500, 3), // confirmed
                    createContribution(USER_ID.toString(), 300, 3), // confirmed
                    createContribution(USER_ID.toString(), 100, 1)  // pledged (not confirmed)
            );
            when(casesGateway.getContributions(USER_ID)).thenReturn(contributions);
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(null);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.confirmed()).isEqualTo(800);
        }

        @Test
        @DisplayName("Should return zero for empty contributions")
        void shouldReturnZeroForEmptyContributions() {
            when(casesGateway.getContributions(USER_ID)).thenReturn(Collections.emptyList());
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(null);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.pledged()).isZero();
            assertThat(result.paid()).isZero();
            assertThat(result.confirmed()).isZero();
        }
    }

    @Nested
    @DisplayName("When user has connections")
    class UserHasConnections {

        @Test
        @DisplayName("Should include connection ledgers sorted by pledged amount descending")
        void shouldIncludeConnectionLedgersSortedByPledgedDescending() {
            UUID connection1Id = UUID.randomUUID();
            UUID connection2Id = UUID.randomUUID();
            
            Member member = new Member(
                    new MemberId(USER_ID),
                    new MemberId(UUID.randomUUID()),
                    Collections.emptyList(),
                    List.of(new MemberId(connection1Id), new MemberId(connection2Id))
            );
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(member);
            
            when(casesGateway.getContributions(USER_ID)).thenReturn(Collections.emptyList());
            
            List<AccountDTO> connections = List.of(
                    createAccountDTO(connection1Id.toString(), "Connection 1"),
                    createAccountDTO(connection2Id.toString(), "Connection 2")
            );
            when(accountGateway.getAccounts(anyList())).thenReturn(connections);
            
            List<ContributionDTO> connectionContributions = List.of(
                    createContribution(connection1Id.toString(), 100, 1), // pledged
                    createContribution(connection2Id.toString(), 500, 1)  // pledged (higher)
            );
            when(casesGateway.getContributions(anyList())).thenReturn(connectionContributions);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.connectionsLedger()).hasSize(2);
            // Should be sorted by pledged descending
            assertThat(result.connectionsLedger().get(0).pledged()).isEqualTo(500);
            assertThat(result.connectionsLedger().get(1).pledged()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should calculate each connection's contributions separately")
        void shouldCalculateEachConnectionContributionsSeparately() {
            UUID connection1Id = UUID.randomUUID();
            UUID connection2Id = UUID.randomUUID();
            
            Member member = new Member(
                    new MemberId(USER_ID),
                    new MemberId(UUID.randomUUID()),
                    Collections.emptyList(),
                    List.of(new MemberId(connection1Id), new MemberId(connection2Id))
            );
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(member);
            
            when(casesGateway.getContributions(USER_ID)).thenReturn(Collections.emptyList());
            
            List<AccountDTO> connections = List.of(
                    createAccountDTO(connection1Id.toString(), "Connection 1"),
                    createAccountDTO(connection2Id.toString(), "Connection 2")
            );
            when(accountGateway.getAccounts(anyList())).thenReturn(connections);
            
            List<ContributionDTO> connectionContributions = List.of(
                    createContribution(connection1Id.toString(), 100, 1), // pledged
                    createContribution(connection1Id.toString(), 200, 2), // paid
                    createContribution(connection2Id.toString(), 300, 3)  // confirmed
            );
            when(casesGateway.getContributions(anyList())).thenReturn(connectionContributions);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            // Find connection 1
            LedgerSummaryDefaultResponse.ConnectionLedger conn1 = result.connectionsLedger().stream()
                    .filter(c -> c.uuid().equals(connection1Id.toString()))
                    .findFirst().orElseThrow();
            assertThat(conn1.pledged()).isEqualTo(100);
            assertThat(conn1.paid()).isEqualTo(200);
            assertThat(conn1.confirmed()).isZero();
            
            // Find connection 2
            LedgerSummaryDefaultResponse.ConnectionLedger conn2 = result.connectionsLedger().stream()
                    .filter(c -> c.uuid().equals(connection2Id.toString()))
                    .findFirst().orElseThrow();
            assertThat(conn2.pledged()).isZero();
            assertThat(conn2.paid()).isZero();
            assertThat(conn2.confirmed()).isEqualTo(300);
        }
    }

    @Nested
    @DisplayName("When user has no connections")
    class UserHasNoConnections {

        @Test
        @DisplayName("Should return empty connections ledger when member not found")
        void shouldReturnEmptyConnectionsLedgerWhenMemberNotFound() {
            when(casesGateway.getContributions(USER_ID)).thenReturn(Collections.emptyList());
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(null);

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.connectionsLedger()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty connections ledger when member has no children")
        void shouldReturnEmptyConnectionsLedgerWhenMemberHasNoChildren() {
            Member member = new Member(
                    new MemberId(USER_ID),
                    new MemberId(UUID.randomUUID()),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
            when(membersNetworkRepo.getById(USER_ID)).thenReturn(member);
            when(casesGateway.getContributions(USER_ID)).thenReturn(Collections.emptyList());
            when(accountGateway.getAccounts(Collections.emptyList())).thenReturn(Collections.emptyList());
            when(casesGateway.getContributions(Collections.emptyList())).thenReturn(Collections.emptyList());

            LedgerSummaryDefaultResponse result = handler.handle(new GetLedgerSummary(USER_ID));

            assertThat(result.connectionsLedger()).isEmpty();
        }
    }

    private ContributionDTO createContribution(String contributorId, int amount, int status) {
        return new ContributionDTO(
                UUID.randomUUID().toString(),
                contributorId,
                12345,
                amount,
                status,
                System.currentTimeMillis(),
                null
        );
    }

    private AccountDTO createAccountDTO(String id, String name) {
        return new AccountDTO(id, "1234567890", name, "photo.jpg", Collections.emptyList());
    }
}
