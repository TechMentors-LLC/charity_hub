package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.queriers.Account;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionResponse;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.charity_hub.accounts.internal.core.queriers.GetConnectionsQuery;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionsHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;

import java.util.List;

@RestController
public class GetConnectionsController {
    private static final Logger log = LoggerFactory.getLogger(GetConnectionsController.class);
    private final GetConnectionsHandler getConnectionsHandler;

    public GetConnectionsController(GetConnectionsHandler getConnectionsHandler) {
        this.getConnectionsHandler = getConnectionsHandler;
    }

    @GetMapping("/v1/accounts/connections")
    @Timed(value = "charity_hub.accounts.get_connections", description = "Time taken to retrieve user connections")
    @Observed(name = "accounts.get_connections", contextualName = "get-connections")
    public ResponseEntity<GetConnectionResponse> handle(@AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        log.debug("Retrieving connections for authenticated user: {}", accessTokenPayload.getUserId());
        GetConnectionsQuery command = new GetConnectionsQuery(accessTokenPayload.getUserId());
        List<Account> accountList = getConnectionsHandler.handle(command);
        log.debug("Retrieved {} connections", accountList.size());
        return ResponseEntity.ok(new GetConnectionResponse(accountList));
    }
}
