package com.charity_hub.accounts.internal.core.queriers;

import com.charity_hub.accounts.internal.core.contracts.IAccountReadRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetConnectionsHandler Tests")
class GetConnectionsHandlerTest {

    @Mock
    private IAccountReadRepo accountReadRepo;

    private GetConnectionsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetConnectionsHandler(accountReadRepo);
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("should return connections for user")
        void shouldReturnConnectionsForUser() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var query = new GetConnectionsQuery(userId);
            var account1 = new Account(UUID.randomUUID().toString(), "John Doe", "http://photo1.url", List.of("CONTRIBUTE"));
            var account2 = new Account(UUID.randomUUID().toString(), "Jane Smith", "http://photo2.url", List.of("FULL_ACCESS"));
            when(accountReadRepo.getConnections(userId)).thenReturn(List.of(account1, account2));

            // Act
            List<Account> result = handler.handle(query);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).fullName()).isEqualTo("John Doe");
            assertThat(result.get(1).fullName()).isEqualTo("Jane Smith");
            verify(accountReadRepo).getConnections(userId);
        }

        @Test
        @DisplayName("should return empty list when user has no connections")
        void shouldReturnEmptyListWhenNoConnections() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var query = new GetConnectionsQuery(userId);
            when(accountReadRepo.getConnections(userId)).thenReturn(List.of());

            // Act
            List<Account> result = handler.handle(query);

            // Assert
            assertThat(result).isEmpty();
            verify(accountReadRepo).getConnections(userId);
        }

        @Test
        @DisplayName("should pass correct userId to repository")
        void shouldPassCorrectUserIdToRepository() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var query = new GetConnectionsQuery(userId);
            when(accountReadRepo.getConnections(userId)).thenReturn(List.of());

            // Act
            handler.handle(query);

            // Assert
            verify(accountReadRepo).getConnections(userId);
        }
    }
}
