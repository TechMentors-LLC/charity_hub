package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmContributionHandler extends CommandHandler<ConfirmContribution, Void> {

    private final ICaseRepo caseRepo;
    private final ILogger logger;

    public ConfirmContributionHandler(ICaseRepo caseRepo, ILogger logger) {
        this.caseRepo = caseRepo;
        this.logger = logger;
    }

    @Override
    @Transactional
    public Void handle(ConfirmContribution command) {

        var contribution = caseRepo.getContributionById(command.contributionId())
                .orElseThrow(() -> {
                    logger.error("Contribution not found with ID {} ", command.contributionId());
                    throw new NotFoundException("Contribution not found with ID " + command.contributionId());
                });

        contribution.confirm();
        caseRepo.save(contribution);
        logger.info("Contribution confirmed and saved with ID {}", command.contributionId());

        return null;
    }
}
