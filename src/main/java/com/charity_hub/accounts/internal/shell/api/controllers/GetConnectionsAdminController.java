package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.queriers.Account;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionResponse;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionsQuery;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionsHandler;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class GetConnectionsAdminController {
    private static final Logger log = LoggerFactory.getLogger(GetConnectionsAdminController.class);
    private final GetConnectionsHandler getConnectionsHandler;

    public GetConnectionsAdminController(GetConnectionsHandler getConnectionsHandler) {
        this.getConnectionsHandler = getConnectionsHandler;
    }

    @GetMapping("v1/accounts/{userId}/connections")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Timed(value = "charity_hub.accounts.get_connections_admin", description = "Time taken to retrieve user connections (admin)")
    @Observed(name = "accounts.get_connections_admin", contextualName = "get-connections-admin")
    public ResponseEntity<GetConnectionResponse> handle(@PathVariable UUID userId) {
        log.debug("Retrieving connections for user (admin): {}", userId);
        GetConnectionsQuery command = new GetConnectionsQuery(userId);
        List<Account> accountList = getConnectionsHandler.handle(command);
        log.debug("Retrieved {} connections for user: {}", accountList.size(), userId);
        return ResponseEntity.ok(new GetConnectionResponse(accountList));
    }
}
