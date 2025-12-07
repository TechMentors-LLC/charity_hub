package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.queries.GetLedgerSummary.GetLedgerSummary;
import com.charity_hub.ledger.internal.application.queries.GetLedgerSummary.GetLedgerSummaryHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GetLedgerSummaryController {
    private final GetLedgerSummaryHandler getLedgerSummaryHandler;

    public GetLedgerSummaryController(GetLedgerSummaryHandler getLedgerSummaryHandler) {
        this.getLedgerSummaryHandler = getLedgerSummaryHandler;
    }

    @GetMapping("/v1/ledger/summary")
    public ResponseEntity<?> handle(@AuthenticationPrincipal AccessTokenPayload accessTokenPayload){
        GetLedgerSummary command = new GetLedgerSummary(accessTokenPayload.getUserId());
        var result = getLedgerSummaryHandler.handle(command);
        return ResponseEntity.ok(result);
    }
}
