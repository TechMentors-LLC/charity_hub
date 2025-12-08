package com.charity_hub.shared.exceptions;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAppException(Exception ex, WebRequest request) {
        HttpStatus status;
        
        if (ex instanceof BusinessRuleException) {
            status = HttpStatus.CONFLICT;
            businessRuleExceptionCounter.increment();
            logger.warn("Business rule violation: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        } else if (ex instanceof BadRequestException) {
            status = HttpStatus.BAD_REQUEST;
            badRequestExceptionCounter.increment();
            logger.warn("Bad request: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        } else if (ex instanceof UnAuthorized) {
            status = HttpStatus.UNAUTHORIZED;
            unauthorizedExceptionCounter.increment();
            logger.warn("Unauthorized access attempt: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        } else if (ex instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            notFoundExceptionCounter.increment();
            logger.debug("Resource not found: {} - Request: {}", ex.getMessage(), request.getDescription(false));
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            internalErrorCounter.increment();
            logger.error("Unexpected error occurred - Request: {}", request.getDescription(false), ex);
        }

        Map<String, String> body = new HashMap<>();
        body.put("description", ex.getMessage());
        
        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(Exception ex) {
        notFoundExceptionCounter.increment();
        logger.debug("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyMap());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        badRequestExceptionCounter.increment();
        logger.warn("Method argument type mismatch: parameter '{}' - {}", ex.getName(), ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("description", "Invalid parameter: " + ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException ex) {
        businessRuleExceptionCounter.increment();
        logger.warn("Business rule exception: {}", ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("description", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}