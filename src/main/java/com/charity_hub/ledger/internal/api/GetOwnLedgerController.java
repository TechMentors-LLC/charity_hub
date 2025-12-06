package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.ledger.internal.application.queries.GetLedger.LedgerResponse;
import com.charity_hub.shared.api.DeferredResults;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class GetOwnLedgerController {
    private final GetLedgerHandler getLedgerHandler;

    public GetOwnLedgerController(GetLedgerHandler getLedgerHandler) {
        this.getLedgerHandler = getLedgerHandler;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/ledger")
    public ResponseEntity<LedgerResponse> handle(
            @AuthenticationPrincipal AccessTokenPayload payload
    ) {
        UUID userId = UUID.fromString(payload.getUuid());
        GetLedger command = new GetLedger(userId);

        LedgerResponse response = getLedgerHandler.handle(command);
        return ResponseEntity.ok(response);
    }
}
