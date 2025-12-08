package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.Authenticate.Authenticate;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateHandler;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateResponse;
import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticateHandler authenticateHandler;
    private final BusinessMetrics businessMetrics;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public AuthController(AuthenticateHandler authenticateHandler, BusinessMetrics businessMetrics) {
        this.authenticateHandler = authenticateHandler;
        this.businessMetrics = businessMetrics;
    }

    @PostMapping("/v1/accounts/authenticate")
    @Timed(value = "charity_hub.authentication.request", description = "Time taken to authenticate user")
    @Observed(name = "authentication.request", contextualName = "authenticate-user")
    public ResponseEntity<AuthenticateResponse> login(@RequestBody Authenticate authenticate) {
        log.info("Processing authentication request");
        businessMetrics.recordAuthenticationAttempt();
        
        try {
            AuthenticateResponse response = authenticateHandler.handle(authenticate);
            businessMetrics.recordAuthenticationSuccess();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            businessMetrics.recordAuthenticationFailure();
            throw e;
        }
     }
}

