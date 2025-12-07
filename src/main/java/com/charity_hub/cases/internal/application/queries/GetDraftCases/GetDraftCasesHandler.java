package com.charity_hub.cases.internal.application.queries.GetDraftCases;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.shared.abstractions.QueryHandlerTemp;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetDraftCasesHandler implements QueryHandlerTemp<GetDraftCases, GetDraftCasesResponse> {
    private final ICaseReadRepo caseRepo;

    public GetDraftCasesHandler(ICaseReadRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public GetDraftCasesResponse handle(GetDraftCases query) {
        List<GetDraftCasesResponse.DraftCase> draftCases = caseRepo.getDraftCasesTemp()
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