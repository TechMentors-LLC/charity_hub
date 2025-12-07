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
public class UnblockUserController {
    private final BlockAccountHandler blockAccountHandler;

    public UnblockUserController(BlockAccountHandler blockAccountHandler) {
        this.blockAccountHandler = blockAccountHandler;
    }

    @PostMapping("/v1/accounts/{userId}/un-block")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<Void> handle(@PathVariable String userId) {
        BlockAccount command = new BlockAccount(userId, true);

        blockAccountHandler.handle(command);

        return ResponseEntity.ok().build();
    }
}