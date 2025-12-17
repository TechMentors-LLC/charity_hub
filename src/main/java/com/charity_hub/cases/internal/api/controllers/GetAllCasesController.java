package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.GetCasesRequest;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetAllCasesQuery;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetCasesQueryResult;
import com.charity_hub.cases.internal.infrastructure.queryhandlers.GetAllCasesHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetAllCasesController {
    private static final Logger log = LoggerFactory.getLogger(GetAllCasesController.class);
    private final GetAllCasesHandler getAllCasesHandler;

    public GetAllCasesController(GetAllCasesHandler getAllCasesHandler) {
        this.getAllCasesHandler = getAllCasesHandler;
    }

    @GetMapping("/v1/cases")
    @Observed(name = "cases.get_all", contextualName = "get-all-cases")
    public ResponseEntity<GetCasesQueryResult> getCases(@ModelAttribute GetCasesRequest request) {
        log.debug("Retrieving cases with offset: {}, limit: {}, onlyZakat: {}", request.offset(), request.limit(),
                request.onlyZakat());
        GetAllCasesQuery query = new GetAllCasesQuery(
                request.code(),
                request.tag(),
                request.content(),
                Math.max(request.offset(), 0),
                Math.min(Math.max(request.limit(), 1), 100),
                request.onlyZakat());
        var result = getAllCasesHandler.handle(query);
        log.debug("Retrieved {} cases", result.cases().size());
        return ResponseEntity.ok(result);
    }
}