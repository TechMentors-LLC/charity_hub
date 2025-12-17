package com.charity_hub.accounts.internal.api.controllers;

import com.charity_hub.accounts.internal.application.commands.BlockAccount.BlockAccount;
import com.charity_hub.accounts.internal.application.commands.BlockAccount.BlockAccountHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockAccountController {
    private static final Logger log = LoggerFactory.getLogger(BlockAccountController.class);
    private final BlockAccountHandler blockAccountHandler;

    public BlockAccountController(BlockAccountHandler blockAccountHandler) {
        this.blockAccountHandler = blockAccountHandler;
    }

    @PostMapping("/v1/accounts/{userId}/block")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Observed(name = "accounts.block", contextualName = "block-account")
    public ResponseEntity<Void> handle(@PathVariable String userId) {
        log.info("Blocking account: {}", userId);
        BlockAccount command = new BlockAccount(userId, false);
        blockAccountHandler.handle(command);
        log.info("Account blocked successfully: {}", userId);
        return ResponseEntity.ok().build();
    }
}