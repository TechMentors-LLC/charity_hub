package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.BlockAccount.BlockAccount;
import com.charity_hub.accounts.internal.core.commands.BlockAccount.BlockAccountHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UnblockUserController {
    private static final Logger log = LoggerFactory.getLogger(UnblockUserController.class);
    private final BlockAccountHandler blockAccountHandler;

    public UnblockUserController(BlockAccountHandler blockAccountHandler) {
        this.blockAccountHandler = blockAccountHandler;
    }

    @PostMapping("/v1/accounts/{userId}/un-block")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Observed(name = "accounts.unblock", contextualName = "unblock-account")
    public ResponseEntity<Void> handle(@PathVariable String userId) {
        log.info("Unblocking account: {}", userId);
        BlockAccount command = new BlockAccount(userId, true);
        blockAccountHandler.handle(command);
        log.info("Account unblocked successfully: {}", userId);
        return ResponseEntity.ok().build();
    }
}