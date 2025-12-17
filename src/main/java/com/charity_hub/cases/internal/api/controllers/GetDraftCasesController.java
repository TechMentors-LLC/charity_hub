package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCases;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesHandler;
import com.charity_hub.cases.internal.application.queries.GetDraftCases.GetDraftCasesResponse;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GetDraftCasesController {
    private static final Logger log = LoggerFactory.getLogger(GetDraftCasesController.class);
    private final GetDraftCasesHandler getDraftCasesHandler;

    public GetDraftCasesController(GetDraftCasesHandler getDraftCasesHandler) {
        this.getDraftCasesHandler = getDraftCasesHandler;
    }

    @GetMapping("/v1/draft-cases")
    @Observed(name = "cases.get_drafts", contextualName = "get-draft-cases")
    public ResponseEntity<GetDraftCasesResponse> handle() {
        log.debug("Retrieving draft cases");
        var response = getDraftCasesHandler.handle(new GetDraftCases());
        log.debug("Retrieved {} draft cases", response.cases().size());
        return ResponseEntity.ok(response);
    }
}