package com.charity_hub.ledger.internal.application.queries.GetLedgerSummary;

import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.infrastructure.gateways.CasesGateway;
import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GetLedgerSummaryHandler implements QueryHandler<GetLedgerSummary, LedgerSummaryDefaultResponse> {
    private final MembersNetworkRepo membersNetworkRepo;
    private final CasesGateway casesGateway;
    private final IAccountGateway accountGateway;

    public GetLedgerSummaryHandler(MembersNetworkRepo membersNetworkRepo, CasesGateway casesGateway, IAccountGateway accountGateway) {
        this.membersNetworkRepo = membersNetworkRepo;
        this.casesGateway = casesGateway;
        this.accountGateway = accountGateway;
    }

    public CompletableFuture<LedgerSummaryDefaultResponse> handle(GetLedgerSummary command) {
        return CompletableFuture.supplyAsync(() -> {
            List<ContributionDTO> userContributions = casesGateway.getContributions(command.userId()).join();

            int pledged = getPledged(userContributions);
            int paid = getPaid(userContributions);
            int confirmed = getConfirmed(userContributions);

            List<AccountDTO> connections =  getConnections(command.userId()).join();
            List<UUID> contributorsIds = connections.stream()
                    .map(connection -> UUID.fromString(connection.id()))
                    .collect(Collectors.toList());

            List<ContributionDTO> allConnectionsContributions =
                    casesGateway.getContributions(contributorsIds).join();

            List<LedgerSummaryDefaultResponse.ConnectionLedger> list = new ArrayList<>();

            for (AccountDTO connection : connections) {
                List<ContributionDTO> contributions = allConnectionsContributions.stream()
                        .filter(contribution ->
                                contribution.contributorId().equals(connection.id()))
                        .collect(Collectors.toList());

                list.add(new LedgerSummaryDefaultResponse.ConnectionLedger(
                        connection.id(),
                        connection.fullName(),
                        connection.photoUrl(),
                        getPledged(contributions),
                        getPaid(contributions),
                        getConfirmed(contributions)
                ));
            }

            list.sort((a, b) -> Integer.compare(b.pledged(), a.pledged()));

            return new LedgerSummaryDefaultResponse(
                    confirmed,
                    pledged,
                    paid,
                    list
            );
        });
    }

    private int getPledged(List<ContributionDTO> contributions) {
        return getTotalAmountByStatus(contributions, 1);
    }

    private int getPaid(List<ContributionDTO> contributions) {
        return getTotalAmountByStatus(contributions, 2);
    }

    private int getConfirmed(List<ContributionDTO> contributions) {
        return getTotalAmountByStatus(contributions, 3);
    }

    private int getTotalAmountByStatus(List<ContributionDTO> contributions, int status) {
        return contributions.stream()
                .filter(contribution -> contribution.status() == status)
                .mapToInt(ContributionDTO::amount)
                .sum();
    }

    private CompletableFuture<List<AccountDTO>> getConnections(UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            Member member = membersNetworkRepo.getById(userId).join();
            if (member == null) {
                return new ArrayList<>();
            }
            return accountGateway.getAccounts(member.children()).join().stream().toList();
        });
    }
}
