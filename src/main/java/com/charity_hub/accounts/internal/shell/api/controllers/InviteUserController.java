package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.InviteAccount.InvitationAccount;
import com.charity_hub.accounts.internal.core.commands.InviteAccount.InviteAccountHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.InviteUserRequest;
import com.charity_hub.shared.auth.AccessTokenPayload;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InviteUserController {
    private static final Logger log = LoggerFactory.getLogger(InviteUserController.class);
    private final InviteAccountHandler inviteAccountHandler;

    public InviteUserController(InviteAccountHandler inviteAccountHandler) {
        this.inviteAccountHandler = inviteAccountHandler;
    }

    @PostMapping("/v1/accounts/invite")
    @Observed(name = "accounts.invite", contextualName = "invite-user")
    public ResponseEntity<Void> handle(
            @Valid @RequestBody InviteUserRequest inviteUserRequest,
            @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        log.info("Inviting user with mobile number: {}", inviteUserRequest.mobileNumber());
        InvitationAccount command = new InvitationAccount(inviteUserRequest.mobileNumber(),
                accessTokenPayload.getUserId());
        inviteAccountHandler.handle(command);
        log.info("User invitation processed successfully");
        return ResponseEntity.ok().build();
    }
}