package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.Contribute.Contribute;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeHandler;
import com.charity_hub.cases.internal.application.commands.Contribute.ContributeDefaultResponse;
import com.charity_hub.cases.internal.api.dtos.ContributeRequest;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContributionController {
    private static final Logger log = LoggerFactory.getLogger(ContributionController.class);
    private final ContributeHandler contributeHandler;
    private final BusinessMetrics businessMetrics;

    public ContributionController(ContributeHandler contributeHandler, BusinessMetrics businessMetrics) {
        this.contributeHandler = contributeHandler;
        this.businessMetrics = businessMetrics;
    }

    @PostMapping("/v1/cases/{caseCode}/contributions")
    @Observed(name = "contributions.create", contextualName = "create-contribution")
    public ResponseEntity<ContributeDefaultResponse> contribute(
            @PathVariable int caseCode,
            @AuthenticationPrincipal AccessTokenPayload accessTokenPayload,
            @RequestBody ContributeRequest contributeRequest) {
        log.info("Processing contribution for case {} from user {}", caseCode, accessTokenPayload.getUserId());
        var command = new Contribute(
                contributeRequest.amount(),
                accessTokenPayload.getUserId(),
                caseCode
        );
        var response = contributeHandler.handle(command);
        businessMetrics.recordContribution();
        log.info("Contribution created successfully: {}", response.getContributionId());
        return ResponseEntity.ok(response);
    }
}