package com.charity_hub.cases.internal.application.queries.GetDraftCases;

import com.charity_hub.cases.internal.infrastructure.repositories.ReadCaseRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class GetDraftCasesHandler implements QueryHandler<GetDraftCases, GetDraftCasesResponse> {
    private final ReadCaseRepo caseRepo;

    public GetDraftCasesHandler(ReadCaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public GetDraftCasesResponse handle(GetDraftCases query) {
        List<GetDraftCasesResponse.DraftCase> draftCases = caseRepo.getDraftCases().join()
                .stream()
                .map(it -> new GetDraftCasesResponse.DraftCase(
                        it.code(),
                        it.title(),
                        it.description(),
                        it.goal(),
                        it.creationDate(),
                        it.lastUpdated(),
                        it.documents()
                ))
                .collect(Collectors.toList());

        return new GetDraftCasesResponse(draftCases);
    }
}