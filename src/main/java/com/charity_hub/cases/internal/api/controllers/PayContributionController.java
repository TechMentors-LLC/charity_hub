package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.PayContributionRequest;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContribution;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContributionHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class PayContributionController {
    private static final Logger log = LoggerFactory.getLogger(PayContributionController.class);
    private final PayContributionHandler payContributionHandler;

    public PayContributionController(PayContributionHandler payContributionHandler){
        this.payContributionHandler = payContributionHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/pay")
    @Observed(name = "contributions.pay", contextualName = "pay-contribution")
    public ResponseEntity<Void> handle(
            @PathVariable UUID contributionId,
            @RequestBody(required = false) PayContributionRequest request) {
        log.info("Processing payment for contribution: {}", contributionId);
        String paymentProof = request != null ? request.PaymentProof() : null;
        PayContribution command = new PayContribution(contributionId, paymentProof);
        payContributionHandler.handle(command);
        log.info("Payment processed successfully for contribution: {}", contributionId);
        return ResponseEntity.ok().build();
    }
}
