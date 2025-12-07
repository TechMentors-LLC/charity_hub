package com.charity_hub.shared.exceptions;

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
        handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(request);
    }

    @Nested
    @DisplayName("When handling application exceptions")
    class AppExceptionHandling {

        @Test
        @DisplayName("Should return CONFLICT for BusinessRuleException")
        void shouldReturnConflictForBusinessRuleException() {
            BusinessRuleException exception = new BusinessRuleException("Business rule violated");

            ResponseEntity<Object> response = handler.handleAppException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isInstanceOf(Map.class);
            @SuppressWarnings("unchecked")
            Map<String, String> body = (Map<String, String>) response.getBody();
            assertThat(body.get("description")).isEqualTo("Business rule violated");
        }

        @Test
        @DisplayName("Should return BAD_REQUEST for BadRequestException")
        void shouldReturnBadRequestForBadRequestException() {
            BadRequestException exception = new BadRequestException("Invalid input");

            ResponseEntity<Object> response = handler.handleAppException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED for UnAuthorized exception")
        void shouldReturnUnauthorizedForUnAuthorizedException() {
            UnAuthorized exception = new UnAuthorized("Access denied");

            ResponseEntity<Object> response = handler.handleAppException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Should return NOT_FOUND for NotFoundException")
        void shouldReturnNotFoundForNotFoundException() {
            NotFoundException exception = new NotFoundException("Resource not found");

            ResponseEntity<Object> response = handler.handleAppException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR for unknown exceptions")
        void shouldReturnInternalServerErrorForUnknownExceptions() {
            RuntimeException exception = new RuntimeException("Unexpected error");

            ResponseEntity<Object> response = handler.handleAppException(exception, webRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("When handling BusinessRuleException specifically")
    class BusinessRuleExceptionHandling {

        @Test
        @DisplayName("Should return CONFLICT with error message")
        void shouldReturnConflictWithErrorMessage() {
            BusinessRuleException exception = new BusinessRuleException("Cannot perform this action");

            ResponseEntity<Map<String, String>> response = handler.handleBusinessRule(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).containsEntry("description", "Cannot perform this action");
        }
    }

    @Nested
    @DisplayName("When handling MethodArgumentTypeMismatchException")
    class TypeMismatchHandling {

        @Test
        @DisplayName("Should return BAD_REQUEST with parameter name")
        void shouldReturnBadRequestWithParameterName() {
            MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                    "invalidValue", String.class, "userId", null, new RuntimeException("type mismatch")
            );

            ResponseEntity<Map<String, String>> response = handler.handleMethodArgumentTypeMismatch(exception);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().get("description")).contains("userId");
        }
    }
}
