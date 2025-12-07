package com.charity_hub.cases.internal.application.commands.PayContribution;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PayContributionHandler extends VoidCommandHandler<PayContribution> {
    private final ICaseRepo caseRepo;
    private final ILogger logger;

    public PayContributionHandler(ICaseRepo caseRepo, ILogger logger) {
        this.caseRepo = caseRepo;
        this.logger = logger;
    }

    @Override
    public void handle(PayContribution command) {
        var contribution = caseRepo.getContributionByIdTemp(command.contributionId())
                .orElseThrow(() -> {
                    logger.error("Contribution not found with ID {} ", command.contributionId());
                    return new NotFoundException("Contribution not found with ID " + command.contributionId());
                });
        contribution.pay(command.paymentProof());
        caseRepo.saveTemp(contribution);
        logger.info("Contribution paid and saved with ID {} ", command.contributionId());
    }
}
