package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution;
import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContributionHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class ConfirmContributionController {
    private final ConfirmContributionHandler confirmContributionHandler;

    ConfirmContributionController(ConfirmContributionHandler confirmContributionHandler){
        this.confirmContributionHandler = confirmContributionHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/confirm")
    public DeferredResult<ResponseEntity<?>> handle(@PathVariable UUID contributionId){
        ConfirmContribution command = new ConfirmContribution(contributionId);
        return DeferredResults.from(confirmContributionHandler
                .handle(command)
                .thenApply(ResponseEntity::ok));
    }
}
