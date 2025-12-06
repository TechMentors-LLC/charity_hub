package com.charity_hub.cases.internal.infrastructure.queryhandlers;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.cases.internal.application.queries.CaseCriteria;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetCasesQueryResult;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetAllCasesQuery;
import com.charity_hub.shared.abstractions.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.charity_hub.cases.internal.application.queries.GetAllCases.GetCasesQueryResult.Case;

@Service
public class GetAllCasesHandler implements QueryHandler<GetAllCasesQuery, GetCasesQueryResult> {
    private final ICaseReadRepo readCaseRepo;

    public GetAllCasesHandler(ICaseReadRepo readCaseRepo) {
        this.readCaseRepo = readCaseRepo;
    }

    @Override
    public GetCasesQueryResult handle(GetAllCasesQuery query) {
        return CompletableFuture.supplyAsync(() -> {

            CaseCriteria filter = new CaseCriteria(
                    query.code(),
                    query.tag(),
                    query.content()
            );

            List<Case> cases = readCaseRepo.search(query.offset(), query.limit(), filter);

            int casesCount = readCaseRepo.getCasesCount(filter).join();

            return new GetCasesQueryResult(cases, casesCount);
        });
    }
}