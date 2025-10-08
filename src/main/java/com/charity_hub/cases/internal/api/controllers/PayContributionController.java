package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.api.dtos.PayContributionRequest;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContribution;
import com.charity_hub.cases.internal.application.commands.PayContribution.PayContributionHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class PayContributionController {
    private final PayContributionHandler payContributionHandler;

    public PayContributionController(PayContributionHandler payContributionHandler){
        this.payContributionHandler = payContributionHandler;
    }

    @PostMapping("/v1/contributions/{contributionId}/pay")
    public DeferredResult<ResponseEntity<?>> handle(
            @PathVariable UUID contributionId,
            @RequestBody(required = false) PayContributionRequest request) {
        String paymentProof = request != null ? request.PaymentProof() : null;
        PayContribution command = new PayContribution(contributionId,paymentProof);
        return DeferredResults.from(payContributionHandler
                .handle(command)
                .thenApply(ResponseEntity::ok));
    }
}
