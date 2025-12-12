package com.charity_hub.accounts.internal.api.controllers;

import com.charity_hub.accounts.internal.application.commands.ChangePermission.ChangePermission;
import com.charity_hub.accounts.internal.application.commands.ChangePermission.ChangePermissionHandler;
import com.charity_hub.accounts.internal.api.dtos.ChangePermissionRequest;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AddUserPermissionController {
    private static final Logger log = LoggerFactory.getLogger(AddUserPermissionController.class);
    private final ChangePermissionHandler changePermissionHandler;

    public AddUserPermissionController(ChangePermissionHandler changePermissionHandler) {
        this.changePermissionHandler = changePermissionHandler;
    }

    @PostMapping("/v1/accounts/{userId}/permissions")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Observed(name = "accounts.add_permission", contextualName = "add-permission")
    public ResponseEntity<Void> handle(
            @PathVariable UUID userId,
            @RequestBody ChangePermissionRequest request) {
        log.info("Adding permission {} to user: {}", request.permission(), userId);
        ChangePermission command = new ChangePermission(userId, request.permission(), true);
        changePermissionHandler.handle(command);
        log.info("Permission {} added successfully to user: {}", request.permission(), userId);
        return ResponseEntity.ok().build();
    }
}