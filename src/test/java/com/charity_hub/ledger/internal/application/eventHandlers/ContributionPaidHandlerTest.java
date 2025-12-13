package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionPaidLogger;
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
@DisplayName("ContributionPaidHandler Tests")
class ContributionPaidHandlerTest {

    @Mock
    private IEventBus eventBus;

    @Mock
    private INotificationService notificationService;

    @Mock
    private ContributionPaidLogger logger;

    private ContributionPaidHandler handler;

    private final UUID CONTRIBUTOR_ID = UUID.randomUUID();
    private final UUID CONTRIBUTION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        handler = new ContributionPaidHandler(eventBus, notificationService, logger);
    }

    @Nested
    @DisplayName("When handler starts")
    class HandlerRegistration {

        @Test
        @DisplayName("Should register handler and subscribe to ContributionPaidDTO events")
        void shouldRegisterHandler() {
            handler.start();

            verify(logger).handlerRegistered();
            verify(eventBus).subscribe(eq(handler), eq(ContributionPaidDTO.class), any());
        }
    }

    @Nested
    @DisplayName("When processing contribution paid event")
    class ProcessingContributionPaid {

        @Test
        @DisplayName("Should send notification on contribution paid")
        void shouldSendNotification() {
            // Given
            ContributionPaidDTO contribution = createContributionPaidDTO();

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).processingEvent(contribution);
            verify(notificationService).notifyContributionPaid(contribution);
            verify(logger).notificationSent(CONTRIBUTION_ID, CONTRIBUTOR_ID);
        }

        @Test
        @DisplayName("Should log event processing correctly")
        void shouldLogProcessingEvent() {
            // Given
            ContributionPaidDTO contribution = createContributionPaidDTO();

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
            ContributionPaidDTO contribution = createContributionPaidDTO();
            doThrow(new RuntimeException("Notification failed")).when(notificationService)
                    .notifyContributionPaid(any());

            // When
            invokeHandle(contribution);

            // Then
            verify(logger).notificationFailed(eq(CONTRIBUTION_ID), eq(CONTRIBUTOR_ID), any(Exception.class));
        }
    }

    private ContributionPaidDTO createContributionPaidDTO() {
        return new ContributionPaidDTO(
                CONTRIBUTION_ID,
                CONTRIBUTOR_ID,
                100);
    }

    /**
     * Uses reflection to invoke the private handle method for testing
     */
    private void invokeHandle(ContributionPaidDTO contribution) {
        try {
            var handleMethod = ContributionPaidHandler.class.getDeclaredMethod("handle", ContributionPaidDTO.class);
            handleMethod.setAccessible(true);
            handleMethod.invoke(handler, contribution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke handle method", e);
        }
    }
}
