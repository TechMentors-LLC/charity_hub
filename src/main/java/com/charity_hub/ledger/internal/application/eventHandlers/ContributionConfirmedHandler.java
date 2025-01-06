package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionConfirmedLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

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

    @Bean("ContributionConfirmedListener")
    public CompletableFuture<Void> start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionConfirmedDTO.class, this::handle);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handle(ContributionConfirmedDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            logger.processingEvent(contribution);

            try {
                notificationService.notifyContributionConfirmed(contribution).join();
                logger.notificationSent(contribution.id(), contribution.contributorId());
            } catch (Exception e) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
            }
        });
    }
}