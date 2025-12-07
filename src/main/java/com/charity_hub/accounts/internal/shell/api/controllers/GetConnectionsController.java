package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.queriers.Account;
import com.charity_hub.accounts.internal.core.queriers.GetConnectionResponse;
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
    private final GetConnectionsHandler getConnectionsHandler;

    public GetConnectionsController(GetConnectionsHandler getConnectionsHandler) {
        this.getConnectionsHandler = getConnectionsHandler;
    }

    @GetMapping("/v1/accounts/connections")
    public ResponseEntity<GetConnectionResponse> handle(@AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        GetConnectionsQuery command = new GetConnectionsQuery(accessTokenPayload.getUserId());

              List<Account> accountList =  getConnectionsHandler.handle(command);

              return ResponseEntity.ok(new GetConnectionResponse(accountList));


    }
}
