package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetOwnLedgerController {
    private final GetLedgerHandler getLedgerHandler;

    public GetOwnLedgerController(GetLedgerHandler getLedgerHandler) {
        this.getLedgerHandler = getLedgerHandler;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/ledger/me")
    public ResponseEntity<?> handle(
            @AuthenticationPrincipal AccessTokenPayload payload
    ) {
        UUID userId = UUID.fromString(payload.getUuid());
        GetLedger command = new GetLedger(userId);
        var result = getLedgerHandler.handle(command);
        return ResponseEntity.ok(result);
    }
}
