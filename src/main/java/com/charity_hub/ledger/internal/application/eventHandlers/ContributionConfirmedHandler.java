package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionConfirmedLogger;
import com.charity_hub.shared.domain.IEventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class ContributionConfirmedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final ContributionConfirmedLogger logger;

    public ContributionConfirmedHandler(
            IEventBus eventBus,
            INotificationService notificationService,
            ContributionConfirmedLogger logger
    ) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionConfirmedDTO.class, this::handle);
    }

    @io.micrometer.core.annotation.Timed(value = "charity_hub.event.contribution_confirmed", description = "Time taken to handle ContributionConfirmed event")
    private void handle(ContributionConfirmedDTO contribution) {
        logger.processingEvent(contribution);

        try {
            notificationService.notifyContributionConfirmed(contribution);
            logger.notificationSent(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
        }
    }
}