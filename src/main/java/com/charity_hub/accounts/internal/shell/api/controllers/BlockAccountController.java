package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.BlockAccount.BlockAccount;
import com.charity_hub.accounts.internal.core.commands.BlockAccount.BlockAccountHandler;
import com.charity_hub.shared.api.DeferredResults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class BlockAccountController {
    private final BlockAccountHandler blockAccountHandler;

    public BlockAccountController(BlockAccountHandler blockAccountHandler) {
        this.blockAccountHandler = blockAccountHandler;
    }

    @PostMapping("/v1/accounts/{userId}/block")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<Void> handle(@PathVariable String userId) {
        blockAccountHandler
                .handle(new BlockAccount(userId, false));
        return ResponseEntity.ok().build();
    }
}