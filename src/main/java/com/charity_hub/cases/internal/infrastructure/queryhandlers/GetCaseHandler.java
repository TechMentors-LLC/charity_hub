package com.charity_hub.cases.internal.infrastructure.queryhandlers;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.cases.internal.application.queries.Contribution;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseResponse;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseQuery;
import com.charity_hub.cases.internal.infrastructure.gateways.AccountsGateway;
import com.charity_hub.cases.internal.infrastructure.repositories.ReadCaseRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.charity_hub.shared.exceptions.NotFoundException;

@Service
public class GetCaseHandler implements QueryHandler<GetCaseQuery, GetCaseResponse> {
    private final ReadCaseRepo caseRepo;
    private final GetCaseMapper getCaseMapper;
    private final AccountsGateway accountsGateway;

    public GetCaseHandler(
            ReadCaseRepo caseRepo,
            GetCaseMapper getCaseMapper, AccountsGateway accountsGateway
    ) {
        this.caseRepo = caseRepo;
        this.getCaseMapper = getCaseMapper;
        this.accountsGateway = accountsGateway;
    }

    @Override
    public GetCaseResponse handle(GetCaseQuery query) {
            var case_ = caseRepo.getByCode(query.caseCode())
                    .orElseThrow(() -> new NotFoundException(String.format("Case with code %s is not found", query.caseCode())));

            var contributions = caseRepo.getContributionsByCaseCode(case_.code());

            // get the contributors details only if the account has full access
            if (query.accessTokenPayload().hasFullAccess()) {

                List<UUID> contributorsIds = contributions
                        .stream()
                        .map(Contribution::contributorId)
                        .toList();

                List<AccountDTO> contributors = accountsGateway.getAccountsByIds(contributorsIds).join();

                var caseDetails = getCaseMapper.toCaseDetails(case_, contributions, contributors);
                return new GetCaseResponse(caseDetails);
            }

            var caseDetails = getCaseMapper.toCaseDetails(case_, contributions, null);
            return new GetCaseResponse(caseDetails);
    }
}