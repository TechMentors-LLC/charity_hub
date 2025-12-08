package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContribution;
import com.charity_hub.cases.internal.application.commands.ConfirmContribution.ConfirmContributionHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ConfirmContributionController {
    private static final Logger log = LoggerFactory.getLogger(ConfirmContributionController.class);
    private final ConfirmContributionHandler confirmContributionHandler;

    public ConfirmContributionController(ConfirmContributionHandler confirmContributionHandler){
        this.confirmContributionHandler = confirmContributionHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/confirm")
    @Observed(name = "contributions.confirm", contextualName = "confirm-contribution")
    public ResponseEntity<Void> handle(@PathVariable UUID contributionId){
        log.info("Confirming contribution: {}", contributionId);
        ConfirmContribution command = new ConfirmContribution(contributionId);
        confirmContributionHandler.handle(command);
        log.info("Contribution confirmed successfully: {}", contributionId);
        return ResponseEntity.ok().build();
    }
}
