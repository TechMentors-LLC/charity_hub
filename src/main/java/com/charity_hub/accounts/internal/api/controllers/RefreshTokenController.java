package com.charity_hub.accounts.internal.api.controllers;

import com.charity_hub.accounts.internal.application.commands.RefreshToken.RefreshToken;
import com.charity_hub.accounts.internal.application.commands.RefreshToken.RefreshTokenHandler;
import com.charity_hub.accounts.internal.application.commands.RefreshToken.RefreshTokenResponse;
import com.charity_hub.shared.auth.RefreshTokenPayload;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class RefreshTokenController {
    private static final Logger log = LoggerFactory.getLogger(RefreshTokenController.class);
    private final RefreshTokenHandler refreshTokenHandler;

    public RefreshTokenController(RefreshTokenHandler refreshTokenHandler) {
        this.refreshTokenHandler = refreshTokenHandler;
    }

    @PostMapping("/v1/accounts/refresh-token")
    @Observed(name = "accounts.refresh_token", contextualName = "refresh-token")
    public ResponseEntity<RefreshTokenResponse> handle(
            @AuthenticationPrincipal RefreshTokenPayload refreshTokenPayload,
            HttpServletRequest request
    ) {
        log.debug("Refreshing token for user: {}", refreshTokenPayload.getUuid());
        // Extract the actual token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        String encodedRefreshToken = authHeader.substring("Bearer ".length());
        
        RefreshToken command = new RefreshToken(
                encodedRefreshToken,  // The actual JWT string
                UUID.fromString(refreshTokenPayload.getUuid()),
                refreshTokenPayload.getDeviceId()
        );

        String refreshToken = refreshTokenHandler.handle(command);
        log.debug("Token refreshed successfully for user: {}", refreshTokenPayload.getUuid());
        return ResponseEntity.ok(new RefreshTokenResponse(refreshToken));
    }
}
