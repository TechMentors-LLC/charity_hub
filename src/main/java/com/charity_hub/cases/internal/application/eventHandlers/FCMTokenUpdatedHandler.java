package com.charity_hub.cases.internal.application.eventHandlers;

import com.charity_hub.accounts.shared.AccountEventDto;
import com.charity_hub.cases.internal.domain.contracts.INotificationService;
import com.charity_hub.cases.internal.application.loggers.FCMTokenLogger;
import com.charity_hub.shared.domain.IEventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class FCMTokenUpdatedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final FCMTokenLogger logger;

    public FCMTokenUpdatedHandler(IEventBus eventBus, INotificationService notificationService, FCMTokenLogger baseLogger) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = baseLogger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, AccountEventDto.FCMTokenUpdatedDTO.class, this::handle);
    }

    public void handle(AccountEventDto.FCMTokenUpdatedDTO event) {
        if (event.deviceFCMToken() == null) {
            logger.nullTokenReceived();
            return;
        }

        logger.processingToken(event.deviceFCMToken());
        
        try {
            notificationService.subscribeAccountToCaseUpdates(event.deviceFCMToken());
            logger.tokenSubscribed(event.deviceFCMToken());
        } catch (Exception e) {
            logger.tokenSubscriptionFailed(event.deviceFCMToken(), e);
        }
    }
}