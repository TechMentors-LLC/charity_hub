package com.charity_hub.cases.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.CaseClosedDTO;
import com.charity_hub.cases.internal.domain.contracts.INotificationService;
import com.charity_hub.cases.internal.application.eventHandlers.loggers.CaseClosedLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.CompletableFuture;

@Service
public class CaseClosedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final CaseClosedLogger logger;

    public CaseClosedHandler(IEventBus eventBus, INotificationService notificationService, CaseClosedLogger logger) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, CaseClosedDTO.class, this::handle);
    }

    private void handle(CaseClosedDTO case_) {
            logger.processingEvent(case_);

            try {
                notificationService.notifyCaseClosed(case_).join();
                logger.notificationSent(case_.caseCode());
            } catch (Exception e) {
                logger.notificationFailed(case_.caseCode(), e);
            }
    }
} 