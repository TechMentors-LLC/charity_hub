package com.charity_hub.ledger.internal.application.queries.GetLedger;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.infrastructure.gateways.CasesGateway;
import com.charity_hub.shared.abstractions.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GetLedgerHandler implements QueryHandler<GetLedger,LedgerResponse> {
    private final CasesGateway casesGateway;

    public GetLedgerHandler(CasesGateway casesGateway) {
        this.casesGateway = casesGateway;
    }

    public CompletableFuture<LedgerResponse> handle(GetLedger command) {
        return CompletableFuture.supplyAsync(() -> {
            var contributions = casesGateway.getContributions(command.userId()).join();
            contributions.sort(Comparator.comparingInt(ContributionDTO::status));

            var caseCodes = contributions
                    .stream()
                    .map(ContributionDTO::caseCode)
                    .toList();

            var cases = casesGateway.getCasesByIds(caseCodes).join();

            var contributionsResponse = contributions.stream()
                    .map(contribution -> new Contribution(
                            contribution.id(),
                            contribution.contributorId(),
                            contribution.caseCode(),
                            getCaseName(cases, contribution.caseCode()),
                            contribution.amount(),
                            contribution.status() == 1 ? "PLEDGED" : "PAID",
                            contribution.contributionDate()
                    ))
                    .collect(Collectors.toList());

            return new LedgerResponse(contributionsResponse);
        });
    }

    private String getCaseName(List<CaseDTO> cases, int code) {
        return cases.stream()
                .filter(case_ -> case_.code() == code)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Case not found for code: " + code))
                .title();
    }
}