package com.charity_hub.cases.internal.application.queries.GetDraftCases;

import com.charity_hub.cases.internal.application.contracts.ICaseReadRepo;
import com.charity_hub.shared.abstractions.QueryHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetDraftCasesHandler implements QueryHandler<GetDraftCases, GetDraftCasesResponse> {
    private final ICaseReadRepo caseRepo;

    public GetDraftCasesHandler(ICaseReadRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Observed(name = "handler.get_draft_cases", contextualName = "get-draft-cases-handler")
    public GetDraftCasesResponse handle(GetDraftCases query) {
        List<GetDraftCasesResponse.DraftCase> draftCases = caseRepo.getDraftCases()
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