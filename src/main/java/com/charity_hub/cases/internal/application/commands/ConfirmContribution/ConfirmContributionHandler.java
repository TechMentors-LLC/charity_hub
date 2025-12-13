package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.cases.shared.IMemberGateway;
import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.infrastructure.gateways.AccountsGateway;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.NotFoundException;
import com.charity_hub.shared.exceptions.UnAuthorized;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class ConfirmContributionHandler extends VoidCommandHandler<ConfirmContribution> {

    private final ICaseRepo caseRepo;
    private final IMemberGateway memberGateway;
    private final AccountsGateway accountsGateway;
    private final ILogger logger;

    public ConfirmContributionHandler(
            ICaseRepo caseRepo,
            IMemberGateway memberGateway,
            AccountsGateway accountsGateway,
            ILogger logger) {
        this.caseRepo = caseRepo;
        this.memberGateway = memberGateway;
        this.accountsGateway = accountsGateway;
        this.logger = logger;
    }

    @Override
    @Observed(name = "handler.confirm_contribution", contextualName = "confirm-contribution-handler")
    public void handle(ConfirmContribution command) {
        var contribution = caseRepo.getContributionById(command.contributionId())
                .orElseGet(() -> {
                    logger.error("Contribution not found with ID {} ", command.contributionId());
                    throw new NotFoundException("Contribution not found with ID " + command.contributionId());
                });

        // Authorization Check
        boolean isParent = memberGateway.isParent(command.userId(), contribution.getContributorId());
        boolean isAdmin = accountsGateway.isAdmin(command.userId());

        if (!isParent && !isAdmin) {
            logger.error("User {} is not authorized to confirm contribution {}", command.userId(),
                    command.contributionId());
            throw new UnAuthorized("Only parent or admin can confirm contribution");
        }

        contribution.confirm();
        caseRepo.save(contribution);
        logger.info("Contribution confirmed and saved with ID {}", command.contributionId());
    }
}
