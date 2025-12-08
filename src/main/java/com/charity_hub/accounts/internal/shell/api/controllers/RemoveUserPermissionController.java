package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.ChangePermission.ChangePermission;
import com.charity_hub.accounts.internal.core.commands.ChangePermission.ChangePermissionHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.ChangePermissionRequest;
import io.micrometer.core.annotation.Timed;
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
public class RemoveUserPermissionController {
    private static final Logger log = LoggerFactory.getLogger(RemoveUserPermissionController.class);
    private final ChangePermissionHandler changePermissionHandler;

    public RemoveUserPermissionController(ChangePermissionHandler changePermissionHandler) {
        this.changePermissionHandler = changePermissionHandler;
    }

    @PostMapping("/v1/accounts/{userId}/remove-permission")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Timed(value = "charity_hub.accounts.remove_permission", description = "Time taken to remove permission")
    @Observed(name = "accounts.remove_permission", contextualName = "remove-permission")
    public ResponseEntity<Void> handle(
            @PathVariable UUID userId,
            @RequestBody ChangePermissionRequest request
    ) {
        log.info("Removing permission {} from user: {}", request.permission(), userId);
        ChangePermission command = new ChangePermission(userId, request.permission(), false);
        changePermissionHandler.handle(command);
        log.info("Permission {} removed successfully from user: {}", request.permission(), userId);
        return ResponseEntity.ok().build();
    }
}