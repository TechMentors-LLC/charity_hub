package com.charity_hub.ledger.internal.api;

import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedger;
import com.charity_hub.ledger.internal.application.queries.GetLedger.GetLedgerHandler;
import com.charity_hub.shared.auth.AccessTokenPayload;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GetLedgerController {
    private static final Logger log = LoggerFactory.getLogger(GetLedgerController.class);
    private final GetLedgerHandler getLedgerHandler;
    private final IMembersNetworkRepo membersNetworkRepo;

    public GetLedgerController(GetLedgerHandler getLedgerHandler, IMembersNetworkRepo membersNetworkRepo) {
        this.getLedgerHandler = getLedgerHandler;
        this.membersNetworkRepo = membersNetworkRepo;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/v1/ledger/{userId}")
    @Observed(name = "ledger.get", contextualName = "get-ledger")
    public ResponseEntity<?> handle(
            @PathVariable UUID userId,
            @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        log.info("Retrieving ledger for user: {}", userId);

        // Allow if admin OR if user is accessing their own ledger OR if parent is
        // accessing child's ledger
        boolean isAdmin = accessTokenPayload.hasFullAccess();
        boolean isOwnLedger = userId.toString().equals(accessTokenPayload.getUuid());
        boolean isParent = membersNetworkRepo.isParentOf(
                UUID.fromString(accessTokenPayload.getUuid()), userId);

        if (!isAdmin && !isOwnLedger && !isParent) {
            log.warn("Access denied: user {} attempted to access ledger of user {}", accessTokenPayload.getUuid(),
                    userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        GetLedger command = new GetLedger(userId);
        var result = getLedgerHandler.handle(command);
        log.debug("Ledger retrieved successfully for user: {}", userId);
        return ResponseEntity.ok(result);
    }
}
