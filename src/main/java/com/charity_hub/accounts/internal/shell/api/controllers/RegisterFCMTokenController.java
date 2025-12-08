package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.RegisterNotificationToken.RegisterNotificationToken;
import com.charity_hub.accounts.internal.core.commands.RegisterNotificationToken.RegisterNotificationTokenHandler;
import com.charity_hub.accounts.internal.shell.api.dtos.RegisterFCMTokenRequest;
import com.charity_hub.shared.auth.AccessTokenPayload;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterFCMTokenController {
    private static final Logger log = LoggerFactory.getLogger(RegisterFCMTokenController.class);
    private final RegisterNotificationTokenHandler registerNotificationTokenHandler;

    public RegisterFCMTokenController(RegisterNotificationTokenHandler registerNotificationTokenHandler) {
        this.registerNotificationTokenHandler = registerNotificationTokenHandler;
    }

    @PostMapping("/v1/accounts/register-fcm-token")
    @Observed(name = "accounts.register_fcm_token", contextualName = "register-fcm-token")
    public ResponseEntity<Void> handle(@RequestBody RegisterFCMTokenRequest request, @AuthenticationPrincipal AccessTokenPayload accessTokenPayload) {
        log.info("Registering FCM token for user: {}", accessTokenPayload.getUserId());
        RegisterNotificationToken command =
                new RegisterNotificationToken(request.fcmToken(),
                        accessTokenPayload.getDeviceId(),
                        accessTokenPayload.getUserId());

                registerNotificationTokenHandler.handle(command);
                log.info("FCM token registered successfully for user: {}", accessTokenPayload.getUserId());
                return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
