package com.charity_hub.ledger.internal.application.queries.GetLedgerSummary;

import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.application.contracts.ICasesGateway;
import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetLedgerSummaryHandler implements QueryHandler<GetLedgerSummary, LedgerSummaryDefaultResponse> {
    private final ILedgerRepository ledgerRepository;
    private final MembersNetworkRepo membersNetworkRepo;
    private final IAccountGateway accountGateway;
    private final ICasesGateway casesGateway;

    // From ContributionEntity: PLEDGED=1, PAID=2, CONFIRMED=3
    private static final int STATUS_PAID = 2;
    private static final int STATUS_CONFIRMED = 3;

    public GetLedgerSummaryHandler(
            ILedgerRepository ledgerRepository,
            MembersNetworkRepo membersNetworkRepo,
            IAccountGateway accountGateway,
            ICasesGateway casesGateway) {
        this.ledgerRepository = ledgerRepository;
        this.membersNetworkRepo = membersNetworkRepo;
        this.accountGateway = accountGateway;
        this.casesGateway = casesGateway;
    }

    @Override
    @Observed(name = "handler.get_ledger_summary", contextualName = "get-ledger-summary-handler")
    public LedgerSummaryDefaultResponse handle(GetLedgerSummary command) {
        // Get user's ledger for dueAmount (includes own pledges + network transfers)
        MemberId memberId = new MemberId(command.userId());
        Ledger userLedger = ledgerRepository.findByMemberId(memberId);
        int dueAmount = userLedger != null ? userLedger.getDueAmount().value() : 0;

        // Get user's contributions stats
        List<ContributionDTO> userContributions = casesGateway.getContributions(command.userId());
        int paidAmount = calculateAmountByStatus(userContributions, STATUS_PAID);
        int confirmedAmount = calculateAmountByStatus(userContributions, STATUS_CONFIRMED);

        // Pledged = What I owe (dueAmount) minus what I've already paid (pending
        // confirmation)
        // This shows: own unpaid pledges + network transfers received
        int pledgedAmount = Math.max(0, dueAmount - paidAmount);

        // Get connections and their ledger summaries
        List<AccountDTO> connections = getConnections(command.userId());
        List<LedgerSummaryDefaultResponse.ConnectionLedger> connectionLedgers = new ArrayList<>();

        if (!connections.isEmpty()) {
            List<UUID> connectionIds = connections.stream().map(c -> UUID.fromString(c.id())).toList();
            // Bulk fetch contributions for connections
            List<ContributionDTO> allConnectionContributions = casesGateway.getContributions(connectionIds);

            Map<String, List<ContributionDTO>> contributionsByUserId = allConnectionContributions.stream()
                    .collect(Collectors.groupingBy(ContributionDTO::contributorId));

            for (AccountDTO connection : connections) {
                MemberId connectionMemberId = new MemberId(UUID.fromString(connection.id()));
                Ledger connectionLedger = ledgerRepository.findByMemberId(connectionMemberId);
                int connectionDueAmount = connectionLedger != null ? connectionLedger.getDueAmount().value() : 0;

                List<ContributionDTO> connContributions = contributionsByUserId.getOrDefault(connection.id(),
                        List.of());
                int connectionPaid = calculateAmountByStatus(connContributions, STATUS_PAID);
                int connectionConfirmed = calculateAmountByStatus(connContributions, STATUS_CONFIRMED);

                // Same formula: pledged = dueAmount - paid
                int connectionPledged = Math.max(0, connectionDueAmount - connectionPaid);

                connectionLedgers.add(new LedgerSummaryDefaultResponse.ConnectionLedger(
                        connection.id(),
                        connection.fullName(),
                        connection.photoUrl(),
                        connectionPledged, // Pledged (unpaid amount owed)
                        connectionPaid, // Paid (awaiting confirmation)
                        connectionConfirmed // Confirmed
                ));
            }
        }

        // Sort by pledged amount descending
        connectionLedgers.sort((a, b) -> Integer.compare(b.pledged(), a.pledged()));

        return new LedgerSummaryDefaultResponse(
                confirmedAmount,
                pledgedAmount,
                paidAmount,
                connectionLedgers);
    }

    private int calculateAmountByStatus(List<ContributionDTO> contributions, int status) {
        return contributions.stream()
                .filter(c -> c.status() == status)
                .mapToInt(ContributionDTO::amount)
                .sum();
    }

    private List<AccountDTO> getConnections(UUID userId) {
        Member member = membersNetworkRepo.getById(userId);
        if (member == null || member.children().isEmpty()) {
            return new ArrayList<>();
        }
        return accountGateway.getAccounts(member.children()).stream().toList();
    }
}
