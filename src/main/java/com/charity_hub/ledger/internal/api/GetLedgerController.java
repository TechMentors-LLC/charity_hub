package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetLedgerController {
    private static final Logger log = LoggerFactory.getLogger(GetLedgerController.class);
    private final GetLedgerHandler getLedgerHandler;

    public GetLedgerController(GetLedgerHandler getLedgerHandler) {
        this.getLedgerHandler = getLedgerHandler;
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/v1/ledger/{userId}")
    @Observed(name = "ledger.get", contextualName = "get-ledger")
    public ResponseEntity<?> handle(@PathVariable UUID userId) {
        log.info("Retrieving ledger for user: {}", userId);
        GetLedger command = new GetLedger(userId);
        var result = getLedgerHandler.handle(command);
        log.debug("Ledger retrieved successfully for user: {}", userId);
        return ResponseEntity.ok(result);
    }
}
