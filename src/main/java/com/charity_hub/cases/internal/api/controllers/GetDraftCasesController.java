package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCases;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesHandler;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GetDraftCasesController {

    private final GetDraftCasesHandler getDraftCasesHandler;

    public GetDraftCasesController(GetDraftCasesHandler getDraftCasesHandler) {
        this.getDraftCasesHandler = getDraftCasesHandler;
    }

    @GetMapping("/v1/draft-cases")
    public ResponseEntity<GetDraftCasesResponse> handle() {
        var response = getDraftCasesHandler.handle(new GetDraftCases());
        return ResponseEntity.ok(response);
    }
}