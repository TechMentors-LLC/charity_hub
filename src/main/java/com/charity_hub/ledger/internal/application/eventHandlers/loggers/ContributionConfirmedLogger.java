package com.charity_hub.ledger.internal.application.eventHandlers.loggers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ContributionConfirmedLogger {
    private final ILogger logger;

    public ContributionConfirmedLogger(ILogger logger) {
        this.logger = logger;
    }

    public void handlerRegistered() {
        logger.info("Registering ContributionConfirmedHandler");
    }

    public void processingEvent(ContributionConfirmedDTO contribution) {
        logger.info("Processing contribution confirmation - Contribution Id: {}, Contributor ID: {}, Amount: {}",
                contribution.id(), contribution.contributorId(), contribution.amount());
    }

    public void settlingChildObligation(UUID contributorId, int amount) {
        logger.debug("Settling child obligation - Contributor: {}, Amount: {}", contributorId, amount);
    }

    public void cascadingToAncestors(UUID contributorId, List<UUID> ancestorIds, int amount) {
        logger.debug("Cascading dueNetworkAmount debit to {} ancestors - Contributor: {}, Amount: {}",
                ancestorIds.size(), contributorId, amount);
    }

    public void ancestorLedgerUpdated(UUID ancestorId, int amount) {
        logger.debug("Updated ancestor ledger - Ancestor: {}, NetworkAmount: {}", ancestorId, amount);
    }

    public void parentObligationCreated(UUID parentId, int amount) {
        logger.debug("Created parent obligation - Parent: {}, DueAmount: +{}", parentId, amount);
    }

    public void eventProcessedSuccessfully(UUID contributionId, UUID contributorId) {
        logger.info("ContributionConfirmed event processed successfully - Contribution: {}, Contributor: {}",
                contributionId, contributorId);
    }

    public void eventProcessingFailed(UUID contributionId, UUID contributorId, Exception e) {
        logger.error("Failed to process ContributionConfirmed event - Contribution: {}, Contributor: {} - Error: {}",
                contributionId, contributorId, e.getMessage(), e);
    }

    public void notificationSent(UUID id, UUID contributorId) {
        logger.info("Successfully sent confirmation notification - Contribution Id: {}, Contributor ID: {}",
                id, contributorId);
    }

    public void notificationFailed(UUID uuid, UUID contributorId, Exception e) {
        logger.error("Failed to send confirmation notification - Contribution Id: {}, Contributor ID: {} - Error: {}",
                uuid, contributorId, e.getMessage(), e);
    }
}