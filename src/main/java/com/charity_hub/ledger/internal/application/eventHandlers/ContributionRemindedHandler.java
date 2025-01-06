package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionRemindedLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ContributionRemindedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final ContributionRemindedLogger logger;

    public ContributionRemindedHandler(
            IEventBus eventBus,
            INotificationService notificationService,
            ContributionRemindedLogger logger
    ) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @Bean("ContributionRemindedListener")
    public CompletableFuture<Void> start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionRemindedDTO.class, this::handle);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handle(ContributionRemindedDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            logger.processingEvent(contribution);
            
            try {
                notificationService.notifyContributorToPay(contribution).join();
                logger.notificationSent(contribution.id(), contribution.contributorId());
            } catch (Exception e) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
            }
        });
    }
}