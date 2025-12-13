package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionRemindedLogger;
import com.charity_hub.shared.domain.IEventBus;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class ContributionRemindedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final ContributionRemindedLogger logger;

    public ContributionRemindedHandler(
            IEventBus eventBus,
            INotificationService notificationService,
            ContributionRemindedLogger logger) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionRemindedDTO.class, this::handle);
    }

    @Observed(name = "ledger.event.contribution_reminded", contextualName = "contribution-reminded-handler")
    private void handle(ContributionRemindedDTO contribution) {
        logger.processingEvent(contribution);

        try {
            notificationService.notifyContributorToPay(contribution);
            logger.notificationSent(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
        }
    }
}