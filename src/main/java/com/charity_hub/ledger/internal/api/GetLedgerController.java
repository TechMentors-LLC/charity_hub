package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

@RestController
public class GetLedgerController {
    private final GetLedgerHandler getLedgerHandler;

    public GetLedgerController(GetLedgerHandler getLedgerHandler) {
        this.getLedgerHandler = getLedgerHandler;
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/v1/ledger/{userId}")
    public DeferredResult<ResponseEntity<?>> handle(@PathVariable UUID userId) {
        GetLedger command = new GetLedger(userId);

        return DeferredResults.from(
                getLedgerHandler.handle(command)
                        .thenApply(ResponseEntity::ok)
        );

    }
}
