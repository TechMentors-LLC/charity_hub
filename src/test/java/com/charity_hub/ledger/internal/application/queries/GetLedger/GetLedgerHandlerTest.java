package com.charity_hub.ledger.internal.application.queries.GetLedger;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.infrastructure.gateways.CasesGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLedgerHandler Tests")
class GetLedgerHandlerTest {

    @Mock
    private CasesGateway casesGateway;

    @InjectMocks
    private GetLedgerHandler handler;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should return empty ledger when no contributions")
    void shouldReturnEmptyLedgerWhenNoContributions() {
        when(casesGateway.getContributions(userId)).thenReturn(new ArrayList<>());

        GetLedger query = new GetLedger(userId);
        LedgerResponse response = handler.handle(query);

        assertThat(response.contributions()).isEmpty();
    }

    @Test
    @DisplayName("Should return ledger with contributions and case names")
    void shouldReturnLedgerWithContributionsAndCaseNames() {
        String contributionId = UUID.randomUUID().toString();
        int caseCode = 12345;
        long date = System.currentTimeMillis();

        List<ContributionDTO> contributions = List.of(
                new ContributionDTO(contributionId, userId.toString(), caseCode, 1000, 1, date, null)
        );
        List<CaseDTO> cases = List.of(
                new CaseDTO(caseCode, "Charity Case", "Description", 10000, 0, 1, true, date, date, Collections.emptyList(), Collections.emptyList(), 0)
        );

        when(casesGateway.getContributions(userId)).thenReturn(new ArrayList<>(contributions));
        when(casesGateway.getCasesByIds(List.of(caseCode))).thenReturn(cases);

        GetLedger query = new GetLedger(userId);
        LedgerResponse response = handler.handle(query);

        assertThat(response.contributions()).hasSize(1);
        Contribution contribution = response.contributions().get(0);
        assertThat(contribution.caseCode()).isEqualTo(caseCode);
        assertThat(contribution.caseName()).isEqualTo("Charity Case");
        assertThat(contribution.amount()).isEqualTo(1000);
        assertThat(contribution.status()).isEqualTo("PLEDGED");
    }

    @Test
    @DisplayName("Should map PAID status correctly")
    void shouldMapPaidStatusCorrectly() {
        String contributionId = UUID.randomUUID().toString();
        int caseCode = 12345;
        long date = System.currentTimeMillis();

        // status = 2 means PAID
        List<ContributionDTO> contributions = List.of(
                new ContributionDTO(contributionId, userId.toString(), caseCode, 500, 2, date, null)
        );
        List<CaseDTO> cases = List.of(
                new CaseDTO(caseCode, "Test Case", "Description", 5000, 0, 1, true, date, date, Collections.emptyList(), Collections.emptyList(), 0)
        );

        when(casesGateway.getContributions(userId)).thenReturn(new ArrayList<>(contributions));
        when(casesGateway.getCasesByIds(List.of(caseCode))).thenReturn(cases);

        GetLedger query = new GetLedger(userId);
        LedgerResponse response = handler.handle(query);

        assertThat(response.contributions().get(0).status()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("Should throw when case not found for contribution")
    void shouldThrowWhenCaseNotFoundForContribution() {
        String contributionId = UUID.randomUUID().toString();
        int caseCode = 12345;
        long date = System.currentTimeMillis();

        List<ContributionDTO> contributions = List.of(
                new ContributionDTO(contributionId, userId.toString(), caseCode, 1000, 1, date, null)
        );

        when(casesGateway.getContributions(userId)).thenReturn(new ArrayList<>(contributions));
        when(casesGateway.getCasesByIds(List.of(caseCode))).thenReturn(new ArrayList<>());

        GetLedger query = new GetLedger(userId);

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Case not found");
    }
}
