package com.charity_hub.ledger.internal.application.queries.GetLedger;

import com.charity_hub.cases.shared.dtos.CaseDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.ledger.internal.domain.model.ServiceType;
import com.charity_hub.ledger.internal.domain.model.TransactionType;
import com.charity_hub.ledger.internal.infrastructure.gateways.CasesGateway;
import com.charity_hub.shared.abstractions.QueryHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GetLedgerHandler implements QueryHandler<GetLedger, LedgerResponse> {
    private final ILedgerRepository ledgerRepository;
    private final CasesGateway casesGateway; // Still needed for case names

    public GetLedgerHandler(ILedgerRepository ledgerRepository, CasesGateway casesGateway) {
        this.ledgerRepository = ledgerRepository;
        this.casesGateway = casesGateway;
    }

    @Override
    @Observed(name = "handler.get_ledger", contextualName = "get-ledger-handler")
    public LedgerResponse handle(GetLedger command) {
        // Get ledger from repository
        MemberId memberId = new MemberId(command.userId());
        Ledger ledger = ledgerRepository.findByMemberId(memberId);

        if (ledger == null) {
            return new LedgerResponse(List.of());
        }

        // Get case codes from transactions for fetching case names
        var caseCodes = ledger.getTransactions().stream()
                .filter(t -> t.getService().serviceType() == ServiceType.CONTRIBUTION)
                .map(this::extractCaseCodeFromTransaction)
                .distinct()
                .toList();

        // Fetch case details for names (if needed)
        List<CaseDTO> cases = caseCodes.isEmpty() ? List.of() : casesGateway.getCasesByIds(caseCodes);

        // Map ledger transactions to contribution view
        var contributionsResponse = ledger.getTransactions().stream()
                .filter(t -> t.getService().serviceType() == ServiceType.CONTRIBUTION)
                .map(transaction -> new Contribution(
                        transaction.getService().serviceTransactionId().value().toString(),
                        transaction.getMemberId().value().toString(),
                        extractCaseCodeFromTransaction(transaction),
                        getCaseName(cases, extractCaseCodeFromTransaction(transaction)),
                        transaction.getAmount().value(),
                        determineStatus(transaction),
                        transaction.getTimestamp().toEpochMilli()))
                .sorted(Comparator.comparing(Contribution::status))
                .collect(Collectors.toList());

        return new LedgerResponse(contributionsResponse);
    }

    private int extractCaseCodeFromTransaction(com.charity_hub.ledger.internal.domain.model.Transaction transaction) {
        // For now, return 0 as we don't store case code in transaction
        // This would need to be enhanced to store case code in Service or Transaction
        return 0;
    }

    private String determineStatus(com.charity_hub.ledger.internal.domain.model.Transaction transaction) {
        // Determine status based on transaction type
        // CREDIT to dueAmount = PLEDGED (promised to pay)
        // DEBIT from dueAmount = PAID (fulfilled obligation)
        return switch (transaction.getType()) {
            case CREDIT -> "PLEDGED";
            case DEBIT -> "PAID";
        };
    }

    private String getCaseName(List<CaseDTO> cases, int code) {
        return cases.stream()
                .filter(case_ -> case_.code() == code)
                .findFirst()
                .map(CaseDTO::title)
                .orElse("Unknown Case");
    }
}