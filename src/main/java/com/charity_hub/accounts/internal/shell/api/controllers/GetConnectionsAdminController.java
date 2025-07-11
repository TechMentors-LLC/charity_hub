package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.queriers.GetConnectionsQuery;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionsHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class GetConnectionsAdminController {
    private final GetConnectionsHandler getConnectionsHandler;

    public GetConnectionsAdminController(GetConnectionsHandler getConnectionsHandler) {
        this.getConnectionsHandler = getConnectionsHandler;
    }

    @GetMapping("v1/accounts/{userId}/connections")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public DeferredResult<ResponseEntity<?>> handle(@PathVariable UUID userId) {
        GetConnectionsQuery command = new GetConnectionsQuery(userId);
        return DeferredResults.from(
                getConnectionsHandler.handle(command)
                        .thenApply(ResponseEntity::ok));
    }
}
