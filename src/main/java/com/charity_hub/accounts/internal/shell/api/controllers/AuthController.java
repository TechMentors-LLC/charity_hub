package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.Authenticate.Authenticate;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class AuthController {
    private final AuthenticateHandler authenticateHandler;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public AuthController(AuthenticateHandler authenticateHandler) {
        this.authenticateHandler = authenticateHandler;
    }

    @PostMapping("/v1/accounts/authenticate")
    public DeferredResult<ResponseEntity<?>> login(@RequestBody Authenticate authenticate) {
        log.info("Processing authentication request");
        return DeferredResults.from(
                authenticateHandler.handle(authenticate)
                        .thenApply(ResponseEntity::ok)
        );
    }
}

