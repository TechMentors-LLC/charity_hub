package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.Authenticate.Authenticate;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateHandler;
import com.charity_hub.accounts.internal.core.commands.Authenticate.AuthenticateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthenticateHandler authenticateHandler;
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public AuthController(AuthenticateHandler authenticateHandler) {
        this.authenticateHandler = authenticateHandler;
    }

    @PostMapping("/v1/accounts/authenticate")
    public ResponseEntity<AuthenticateResponse> login(@RequestBody Authenticate authenticate) {
        log.info("Processing authentication request");
                AuthenticateResponse response = authenticateHandler.handle(authenticate);
                return ResponseEntity.ok(response);

     }
}

