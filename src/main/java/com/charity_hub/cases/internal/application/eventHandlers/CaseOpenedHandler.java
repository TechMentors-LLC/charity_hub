package com.charity_hub.cases.internal.application.eventHandlers;

import com.charity_hub.cases.internal.application.eventHandlers.loggers.CaseOpenedLogger;
import com.charity_hub.cases.shared.dtos.CaseOpenedDTO;
import com.charity_hub.cases.internal.domain.contracts.INotificationService;
import com.charity_hub.shared.domain.IEventBus;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class CaseOpenedHandler {
    private final IEventBus eventBus;
    private final INotificationService notificationService;
    private final CaseOpenedLogger logger;

    public CaseOpenedHandler(IEventBus eventBus, INotificationService notificationService, CaseOpenedLogger logger) {
        this.eventBus = eventBus;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, CaseOpenedDTO.class, this::handle);
    }

    @io.micrometer.core.annotation.Timed(value = "charity_hub.event.case_opened", description = "Time taken to handle CaseOpened event")
    private void handle(CaseOpenedDTO case_) {
        logger.processingEvent(case_);

        try {
            notificationService.notifyCaseOpened(case_);
            logger.notificationSent(case_.caseCode());
        } catch (Exception e) {
            logger.notificationFailed(case_.caseCode(), e);
        }
    }
} 