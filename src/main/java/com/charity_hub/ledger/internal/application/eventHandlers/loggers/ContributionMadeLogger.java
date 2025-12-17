package com.charity_hub.ledger.internal.application.eventHandlers.loggers;

import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("ledgerContributionMadeLogger")
public class ContributionMadeLogger {
    private final ILogger logger;

    public ContributionMadeLogger(ILogger logger) {
        this.logger = logger;
    }

    public void handlerRegistered() {
        logger.info("Registering ContributionMadeHandler");
    }

    public void processingEvent(ContributionMadeDTO contribution) {
        logger.info("Processing contribution creation - Contribution Id: {}, Contributor ID: {}, Amount: {}",
                contribution.id(), contribution.contributorId(), contribution.amount());
    }

    public void creditingChildLedger(UUID contributorId, int amount) {
        logger.debug("Crediting child ledger - Contributor: {}, Amount: {}", contributorId, amount);
    }

    public void cascadingToAncestors(UUID contributorId, List<UUID> ancestorIds, int amount) {
        logger.debug("Cascading dueNetworkAmount to {} ancestors - Contributor: {}, Amount: {}",
                ancestorIds.size(), contributorId, amount);
    }

    public void ancestorLedgerUpdated(UUID ancestorId, int amount) {
        logger.debug("Updated ancestor ledger - Ancestor: {}, NetworkAmount: +{}", ancestorId, amount);
    }

    public void eventProcessedSuccessfully(UUID contributionId, UUID contributorId) {
        logger.info("ContributionMade event processed successfully - Contribution: {}, Contributor: {}",
                contributionId, contributorId);
    }

    public void eventProcessingFailed(UUID contributionId, UUID contributorId, Exception e) {
        logger.error("Failed to process ContributionMade event - Contribution: {}, Contributor: {} - Error: {}",
                contributionId, contributorId, e.getMessage(), e);
    }
}
