package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetLedgerController {
    private final GetLedgerHandler getLedgerHandler;

    public GetLedgerController(GetLedgerHandler getLedgerHandler) {
        this.getLedgerHandler = getLedgerHandler;
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/v1/ledger/{userId}")
    public ResponseEntity<?> handle(@PathVariable UUID userId) {
        GetLedger command = new GetLedger(userId);
        var result = getLedgerHandler.handle(command);
        return ResponseEntity.ok(result);
    }
}
