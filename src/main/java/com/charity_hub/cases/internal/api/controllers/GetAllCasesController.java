package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.GetCasesRequest;
import com.charity_hub.cases.internal.application.queries.GetAllCases.GetAllCasesQuery;
import com.charity_hub.cases.internal.infrastructure.queryhandlers.GetAllCasesHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class GetAllCasesController {

    private final GetAllCasesHandler getAllCasesHandler;

    public GetAllCasesController(GetAllCasesHandler getAllCasesHandler) {
        this.getAllCasesHandler = getAllCasesHandler;
    }

    @GetMapping("/v1/cases")
    public DeferredResult<ResponseEntity<?>> getCases(@ModelAttribute GetCasesRequest request) {
        GetAllCasesQuery query = new GetAllCasesQuery(
                request.code(),
                request.tag(),
                request.content(),
                Math.max(request.offset(), 0),
                Math.min(Math.max(request.limit(), 1), 100)
        );
        return DeferredResults.from(getAllCasesHandler.handle(query).thenApply(ResponseEntity::ok));
    }
}