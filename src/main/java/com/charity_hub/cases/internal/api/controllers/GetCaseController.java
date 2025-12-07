package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseQuery;
import com.charity_hub.cases.internal.application.queries.GetCase.GetCaseResponse;
import com.charity_hub.cases.internal.application.queries.GetCase.IGetCaseHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetCaseController {

    private final IGetCaseHandler getCaseHandler;

    public GetCaseController(IGetCaseHandler getCaseHandler) {
        this.getCaseHandler = getCaseHandler;
    }

    @GetMapping("/v1/cases/{caseCode}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<GetCaseResponse> getCase(
            @PathVariable int caseCode,
            @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {

        var response = getCaseHandler.handle(new GetCaseQuery(caseCode, accessTokenPayload));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}