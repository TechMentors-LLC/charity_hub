package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionRemindedLogger;
import com.charity_hub.shared.domain.IEventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributionRemindedHandler Tests")
class ContributionRemindedHandlerTest {

    @Mock
    private IEventBus eventBus;

    @Mock
    private INotificationService notificationService;

    @Mock
    private ContributionRemindedLogger logger;

    private ContributionRemindedHandler handler;

    private final UUID CONTRIBUTOR_ID = UUID.randomUUID();
    private final UUID CONTRIBUTION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new ContributionRemindedHandler(eventBus, notificationService, logger);
    }

    @Nested
    @DisplayName("When handler starts")
    class HandlerRegistration {

        @Test
        @DisplayName("Should register handler and subscribe to ContributionRemindedDTO events")
        void shouldRegisterHandler() {
            handler.start();

            verify(logger).handlerRegistered();
            verify(eventBus).subscribe(eq(handler), eq(ContributionRemindedDTO.class), any());
        }
    }

    @Nested
    @DisplayName("When processing contribution reminded event")
    class ProcessingContributionReminded {

        @Test
        @DisplayName("Should send reminder notification")
        void shouldSendReminderNotification() {
            // Given
            ContributionRemindedDTO contribution = createContributionRemindedDTO();

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).processingEvent(contribution);
            verify(notificationService).notifyContributorToPay(contribution);
            verify(logger).notificationSent(CONTRIBUTION_ID, CONTRIBUTOR_ID);
        }

        @Test
        @DisplayName("Should log event processing correctly")
        void shouldLogProcessingEvent() {
            // Given
            ContributionRemindedDTO contribution = createContributionRemindedDTO();

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).processingEvent(contribution);
        }
    }

    @Nested
    @DisplayName("When notification fails")
    class NotificationFailure {

        @Test
        @DisplayName("Should handle notification failure gracefully")
        void shouldHandleNotificationFailure() {
            // Given
            ContributionRemindedDTO contribution = createContributionRemindedDTO();
            doThrow(new RuntimeException("Notification failed")).when(notificationService)
                    .notifyContributorToPay(any());

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).notificationFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID), any(Exception.class));
        }
    }

    private ContributionRemindedDTO createContributionRemindedDTO() {
        return new ContributionRemindedDTO(
                CONTRIBUTION_ID,
                CONTRIBUTOR_ID);
    }

    /**
     * Uses reflection to invoke the private handle method for testing
     */
    private void invokeHandle(ContributionRemindedDTO contribution) {
        try {
            var handleMethod = ContributionRemindedHandler.class.getDeclaredMethod("handle",
                    ContributionRemindedDTO.class);
            handleMethod.setAccessible(true);
            handleMethod.invoke(handler, contribution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handle method", e);
        }
    }
}
