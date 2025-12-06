package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.ChangePermission.ChangePermission;
import com.charity_hub.accounts.internal.core.commands.ChangePermission.ChangePermissionHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.ChangePermissionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AddUserPermissionController {
    private final ChangePermissionHandler changePermissionHandler;

    public AddUserPermissionController(ChangePermissionHandler changePermissionHandler) {
        this.changePermissionHandler = changePermissionHandler;
    }

    @PostMapping("/v1/accounts/{userId}/add-permission")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<Void> handle(
            @PathVariable UUID userId,
            @RequestBody ChangePermissionRequest request
    ) {
        changePermissionHandler.handle(new ChangePermission(userId, request.permission(), true));
        return ResponseEntity.noContent().build();
    }
}