package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.PayRequest;
import com.charity_hub.shared.api.DeferredResults;
import com.charity_hub.cases.internal.application.commands.ChangeContributionStatus.ChangeContributionStatus;
import com.charity_hub.cases.internal.application.commands.ChangeContributionStatus.ChangeContributionStatusHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class ChangeContributionStatusController {

    private final ChangeContributionStatusHandler contributionStatusHandler;

    public ChangeContributionStatusController(ChangeContributionStatusHandler contributionStatusHandler) {
        this.contributionStatusHandler = contributionStatusHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/pay")
    public DeferredResult<ResponseEntity<?>> pay(@PathVariable UUID contributionId, @RequestBody PayRequest request) {
        ChangeContributionStatus command = new ChangeContributionStatus(contributionId,request.proofUrl(),true);
        return DeferredResults.from(contributionStatusHandler
                .handle(command)
                .thenApply(ResponseEntity::ok));
    }

    @PostMapping("/v1/contributions/{contributionId}/confirm")
    public DeferredResult<ResponseEntity<?>> confirm(@PathVariable UUID contributionId) {
        return DeferredResults.from(contributionStatusHandler
                .handle(new ChangeContributionStatus(contributionId, false))
                .thenApply(ResponseEntity::ok));
    }
}