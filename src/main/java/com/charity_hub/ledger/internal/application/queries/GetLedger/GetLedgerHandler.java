package com.charity_hub.ledger.internal.application.queries.GetLedger;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.cases.shared.dtos.ContributionDTO;
import com.charity_hub.ledger.internal.application.contracts.ICasesGateway;
import com.charity_hub.shared.abstractions.QueryHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetLedgerHandler implements QueryHandler<GetLedger, LedgerResponse> {
    private final ICasesGateway casesGateway;

    // From ContributionEntity: PLEDGED=1, PAID=2, CONFIRMED=3
    private static final int STATUS_PLEDGED = 1;
    private static final int STATUS_PAID = 2;
    private static final int STATUS_CONFIRMED = 3;

    public GetLedgerHandler(ICasesGateway casesGateway) {
        this.casesGateway = casesGateway;
    }

    @Override
    @Observed(name = "handler.get_ledger", contextualName = "get-ledger-handler")
    public LedgerResponse handle(GetLedger command) {
        // Fetch all contributions for the user
        List<ContributionDTO> contributions = casesGateway.getContributions(command.userId());

        if (contributions.isEmpty()) {
            return new LedgerResponse(List.of());
        }

        // Collect case codes to fetch titles
        List<Integer> caseCodes = contributions.stream()
                .map(ContributionDTO::caseCode)
                .distinct()
                .toList();

        List<CaseDTO> cases = casesGateway.getCasesByIds(caseCodes);
        Map<Integer, String> caseTitles = cases.stream()
                .collect(Collectors.toMap(CaseDTO::code, CaseDTO::title));

        // Map to response
        List<Contribution> responseContributions = contributions.stream()
                .map(dto -> new Contribution(
                        dto.id(),
                        dto.contributorId(),
                        dto.caseCode(),
                        caseTitles.getOrDefault(dto.caseCode(), "Unknown Case"),
                        dto.amount(),
                        mapStatus(dto.status()),
                        dto.contributionDate()))
                .sorted(Comparator.comparing(Contribution::contributionDate).reversed())
                .collect(Collectors.toList());

        return new LedgerResponse(responseContributions);
    }

    private String mapStatus(int status) {
        return switch (status) {
            case STATUS_PLEDGED -> "PLEDGED";
            case STATUS_PAID -> "PAID";
            case STATUS_CONFIRMED -> "CONFIRMED";
            default -> "UNKNOWN";
        };
    }
}