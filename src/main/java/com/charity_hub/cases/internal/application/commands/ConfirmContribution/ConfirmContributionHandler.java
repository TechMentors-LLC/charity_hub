package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ConfirmContributionHandler extends VoidCommandHandler<ConfirmContribution> {

    private final ICaseRepo caseRepo;
    private final ILogger logger;

    public ConfirmContributionHandler(ICaseRepo caseRepo, ILogger logger) {
        this.caseRepo = caseRepo;
        this.logger = logger;
    }

    @Override
    public void handle(ConfirmContribution command) {

            var contribution = caseRepo.getContributionById(command.contributionId())
                    .orElseGet(()->{
                        logger.error("Contribution not found with ID {} ", command.contributionId());
                        throw new NotFoundException("Contribution not found with ID " + command.contributionId());
                    });
            contribution.confirm();
            caseRepo.save(contribution);
            logger.info("Contribution confirmed and saved with ID {}", command.contributionId());

    }
}
