package com.charity_hub.accounts.internal.shell.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.charity_hub.accounts.internal.core.queriers.GetConnectionsQuery;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionsHandler;
import com.charity_hub.shared.api.DeferredResults;
import com.charity_hub.shared.auth.AccessTokenPayload;

@RestController
public class GetConnectionsController {
    private final GetConnectionsHandler getConnectionsHandler;

    public GetConnectionsController(GetConnectionsHandler getConnectionsHandler) {
        this.getConnectionsHandler = getConnectionsHandler;
    }

    @GetMapping("/v1/accounts/connections")
    public DeferredResult<ResponseEntity<?>> handle(@AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        GetConnectionsQuery command = new GetConnectionsQuery(accessTokenPayload.getUserId());
        return DeferredResults.from(
                getConnectionsHandler.handle(command)
                        .thenApply(ResponseEntity::ok));
    }
}
