package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution;
import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContributionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ConfirmContributionController {
    private final ConfirmContributionHandler confirmContributionHandler;

    public ConfirmContributionController(ConfirmContributionHandler confirmContributionHandler){
        this.confirmContributionHandler = confirmContributionHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/confirm")
    public ResponseEntity<Void> handle(@PathVariable UUID contributionId){
        ConfirmContribution command = new ConfirmContribution(contributionId);
        confirmContributionHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}
