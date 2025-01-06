package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionPaidLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ContributionPaidHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final ContributionPaidLogger logger;

    public ContributionPaidHandler(
            IEventBus eventBus,
            INotificationService notificationService,
            ContributionPaidLogger logger
    ) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @Bean("ContributionPaidListener")
    public CompletableFuture<Void> start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionPaidDTO.class, this::handle);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handle(ContributionPaidDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            logger.processingEvent(contribution);

            try {
                notificationService.notifyContributionPaid(contribution).join();
                logger.notificationSent(contribution.id(), contribution.contributorId());
            } catch (Exception e) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
            }
        });
    }
}