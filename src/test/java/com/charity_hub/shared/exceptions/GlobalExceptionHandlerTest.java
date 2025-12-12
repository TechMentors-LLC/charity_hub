package com.charity_hub.shared.exceptions;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(new SimpleMeterRegistry());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(request);
    }

    @Nested
    @DisplayName("When handling BusinessRuleException")
    class BusinessRuleExceptionHandling {

        @Test
        @DisplayName("Should return CONFLICT for BusinessRuleException")
        void shouldReturnConflictForBusinessRuleException() {
            BusinessRuleException exception = new BusinessRuleException("Business rule violated");

            ResponseEntity<Map<String, String>> response = handler.handleBusinessRule(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("description", "Business rule violated");
        }
    }

    @Nested
    @DisplayName("When handling BadRequestException")
    class BadRequestExceptionHandling {

        @Test
        @DisplayName("Should return BAD_REQUEST for BadRequestException")
        void shouldReturnBadRequestForBadRequestException() {
            BadRequestException exception = new BadRequestException("Invalid input");

            ResponseEntity<Map<String, String>> response = handler.handleBadRequest(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("description")).contains("Invalid input");
        }
    }

    @Nested
    @DisplayName("When handling UnAuthorized exception")
    class UnAuthorizedExceptionHandling {

        @Test
        @DisplayName("Should return UNAUTHORIZED for UnAuthorized exception")
        void shouldReturnUnauthorizedForUnAuthorizedException() {
            UnAuthorized exception = new UnAuthorized("Access denied");

            ResponseEntity<Map<String, String>> response = handler.handleUnauthorized(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).containsEntry("description", "Unauthorized: Access denied");
        }
    }

    @Nested
    @DisplayName("When handling NotFoundException")
    class NotFoundExceptionHandling {

        @Test
        @DisplayName("Should return NOT_FOUND for NotFoundException")
        void shouldReturnNotFoundForNotFoundException() {
            NotFoundException exception = new NotFoundException("Resource not found");

            ResponseEntity<Map<String, String>> response = handler.handleNotFound(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).containsEntry("description", "Not Found: Resource not found");
        }
    }

    @Nested
    @DisplayName("When handling generic Exception")
    class GenericExceptionHandling {

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR for unknown exceptions")
        void shouldReturnInternalServerErrorForUnknownExceptions() {
            RuntimeException exception = new RuntimeException("Unexpected error");

            ResponseEntity<Map<String, String>> response = handler.handleGenericException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).containsEntry("description", "An unexpected error occurred");
        }
    }

    @Nested
    @DisplayName("When handling RateLimitExceededException")
    class RateLimitExceptionHandling {

        @Test
        @DisplayName("Should return TOO_MANY_REQUESTS for RateLimitExceededException")
        void shouldReturnTooManyRequestsForRateLimitException() {
            RateLimitExceededException exception = new RateLimitExceededException("Rate limit exceeded");

            ResponseEntity<Map<String, String>> response = handler.handleRateLimitExceeded(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody()).containsEntry("description", "Rate limit exceeded");
        }
    }

    @Nested
    @DisplayName("When handling MethodArgumentTypeMismatchException")
    class TypeMismatchHandling {

        @Test
        @DisplayName("Should return BAD_REQUEST with parameter name")
        void shouldReturnBadRequestWithParameterName() {
            MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                    "invalidValue", String.class, "userId", null, new RuntimeException("type mismatch"));

            ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentTypeMismatch(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("description")).contains("userId");
        }
    }
}
