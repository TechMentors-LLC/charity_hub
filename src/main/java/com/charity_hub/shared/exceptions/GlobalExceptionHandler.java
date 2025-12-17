package com.charity_hub.shared.exceptions;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Counter businessRuleExceptionCounter;
    private final Counter badRequestExceptionCounter;
    private final Counter unauthorizedExceptionCounter;
    private final Counter notFoundExceptionCounter;
    private final Counter internalErrorCounter;
    private final Counter rateLimitExceptionCounter;

    public GlobalExceptionHandler(MeterRegistry registry) {
        this.businessRuleExceptionCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "business_rule")
                .description("Number of business rule exceptions")
                .register(registry);
        this.badRequestExceptionCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "bad_request")
                .description("Number of bad request exceptions")
                .register(registry);
        this.unauthorizedExceptionCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "unauthorized")
                .description("Number of unauthorized exceptions")
                .register(registry);
        this.notFoundExceptionCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "not_found")
                .description("Number of not found exceptions")
                .register(registry);
        this.internalErrorCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "internal_error")
                .description("Number of internal server errors")
                .register(registry);
        this.rateLimitExceptionCounter = Counter.builder("charity_hub.exceptions")
                .tag("type", "rate_limit")
                .description("Number of rate limit exceptions")
                .register(registry);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        businessRuleExceptionCounter.increment();
        logger.warn("Business rule violation: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex, WebRequest request) {
        badRequestExceptionCounter.increment();
        logger.warn("Bad request: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnAuthorized.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnAuthorized ex, WebRequest request) {
        unauthorizedExceptionCounter.increment();
        logger.warn("Unauthorized access attempt: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex, WebRequest request) {
        notFoundExceptionCounter.increment();
        logger.debug("Resource not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(NoResourceFoundException ex) {
        notFoundExceptionCounter.increment();
        logger.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyMap());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        badRequestExceptionCounter.increment();
        logger.warn("Method argument type mismatch: parameter '{}' - {}", ex.getName(), ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("description", "Invalid parameter: " + ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        badRequestExceptionCounter.increment();
        logger.warn("Validation failed: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("description", "Validation failed");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        response.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimitExceeded(RateLimitExceededException ex) {
        rateLimitExceptionCounter.increment();
        logger.warn("Rate limit exceeded: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex, WebRequest request) {
        internalErrorCounter.increment();
        logger.error("Unexpected error occurred - Request: {}", request.getDescription(false), ex);
        Map<String, String> error = new HashMap<>();
        error.put("description", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}