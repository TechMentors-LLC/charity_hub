package com.charity_hub.cases.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;
import com.charity_hub.cases.internal.domain.contracts.INotificationService;
import com.charity_hub.cases.internal.application.eventHandlers.loggers.ContributionMadeLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ContributionMadeHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final ContributionMadeLogger logger;

    public ContributionMadeHandler(IEventBus eventBus, INotificationService notificationService, ContributionMadeLogger logger) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @Bean("ContributionMadeListener")
    public CompletableFuture<Void> start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionMadeDTO.class, this::handle);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> handle(ContributionMadeDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            logger.processingEvent(contribution);

            try {
                notificationService.notifyContributionMade(contribution).join();
                logger.notificationSent(contribution.caseCode(), contribution.amount());
            } catch (Exception e) {
                logger.notificationFailed(contribution.caseCode(), contribution.amount(), e);
            }
        });
    }
} 