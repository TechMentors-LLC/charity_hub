package com.charity_hub.ledger.internal.application.queries.GetLedgerSummary;

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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetLedgerSummaryHandler implements QueryHandler<GetLedgerSummary, LedgerSummaryDefaultResponse> {
    private final ILedgerRepository ledgerRepository;
    private final MembersNetworkRepo membersNetworkRepo;
    private final IAccountGateway accountGateway;

    public GetLedgerSummaryHandler(
            ILedgerRepository ledgerRepository,
            MembersNetworkRepo membersNetworkRepo,
            IAccountGateway accountGateway) {
        this.ledgerRepository = ledgerRepository;
        this.membersNetworkRepo = membersNetworkRepo;
        this.accountGateway = accountGateway;
    }

    @Override
    @Observed(name = "handler.get_ledger_summary", contextualName = "get-ledger-summary-handler")
    public LedgerSummaryDefaultResponse handle(GetLedgerSummary command) {
        // Get user's ledger
        MemberId memberId = new MemberId(command.userId());
        Ledger userLedger = ledgerRepository.findByMemberId(memberId);

        int dueAmount = userLedger != null ? userLedger.getDueAmount().value() : 0;
        int dueNetworkAmount = userLedger != null ? userLedger.getDueNetworkAmount().value() : 0;

        // Get connections and their ledger summaries
        List<AccountDTO> connections = getConnections(command.userId());
        List<LedgerSummaryDefaultResponse.ConnectionLedger> connectionLedgers = new ArrayList<>();

        for (AccountDTO connection : connections) {
            MemberId connectionId = new MemberId(UUID.fromString(connection.id()));
            Ledger connectionLedger = ledgerRepository.findByMemberId(connectionId);

            int connectionDue = connectionLedger != null ? connectionLedger.getDueAmount().value() : 0;
            int connectionNetwork = connectionLedger != null ? connectionLedger.getDueNetworkAmount().value() : 0;

            connectionLedgers.add(new LedgerSummaryDefaultResponse.ConnectionLedger(
                    connection.id(),
                    connection.fullName(),
                    connection.photoUrl(),
                    connectionDue, // What they owe their parent
                    0, // paid (not tracked separately anymore)
                    connectionNetwork // What they expect from their network
            ));
        }

        // Sort by due amount descending
        connectionLedgers.sort((a, b) -> Integer.compare(b.pledged(), a.pledged()));

        return new LedgerSummaryDefaultResponse(
                dueNetworkAmount, // User expects from network
                dueAmount, // User owes to parent
                0, // paid (not separately tracked)
                connectionLedgers);
    }

    private List<AccountDTO> getConnections(UUID userId) {
        Member member = membersNetworkRepo.getById(userId);
        if (member == null) {
            return new ArrayList<>();
        }
        return accountGateway.getAccounts(member.children()).stream().toList();
    }
}
