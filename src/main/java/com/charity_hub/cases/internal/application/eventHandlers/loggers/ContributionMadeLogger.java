package com.charity_hub.cases.internal.application.eventHandlers.loggers;

import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Component;

@Component("casesContributionMadeLogger")
public class ContributionMadeLogger {
    private final ILogger logger;

    public ContributionMadeLogger(ILogger logger) {
        this.logger = logger;
    }

    public void handlerRegistered() {
        logger.info("Registering ContributionMadeHandler");
    }

    public void processingEvent(ContributionMadeDTO contribution) {
        logger.info("Processing ContributionMadeEvent - Case Code: {}, Amount: {}",
                contribution.caseCode(), contribution.amount());
    }

    public void notificationSent(int caseCode, int amount) {
        logger.info("Successfully sent notification for contribution - Case Code: {}, Amount: {}",
                caseCode, amount);
    }

    public void notificationFailed(int caseCode, int amount, Exception e) {
        logger.error("Failed to send notification for contribution - Case Code: {}, Amount: {} - Error: {}",
                caseCode, amount, e.getMessage(), e);
    }
}