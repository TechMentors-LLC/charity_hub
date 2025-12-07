package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.Contribute.Contribute;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeHandler;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeDefaultResponse;
import com.charity_hub.cases.internal.api.dtos.ContributeRequest;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContributionController {

    private final ContributeHandler contributeHandler;

    public ContributionController(ContributeHandler contributeHandler) {
        this.contributeHandler = contributeHandler;
    }

    @PostMapping("/v1/cases/{caseCode}/contributions")
    public ResponseEntity<ContributeDefaultResponse> contribute(
            @PathVariable int caseCode,
            @AuthenticationPrincipal AccessTokenPayload accessTokenPayload,
            @RequestBody ContributeRequest contributeRequest) {

        var command = new Contribute(
                contributeRequest.amount(),
                accessTokenPayload.getUserId(),
                caseCode
        );
        var response = contributeHandler.handle(command);
        return ResponseEntity.ok(response);
    }
}