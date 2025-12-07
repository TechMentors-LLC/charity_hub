package com.charity_hub.accounts.internal.shell.api.controllers;

import com.charity_hub.accounts.internal.core.commands.RefreshToken.RefreshToken;
import com.charity_hub.accounts.internal.core.commands.RefreshToken.RefreshTokenHandler;
import com.charity_hub.accounts.internal.core.commands.RefreshToken.RefreshTokenResponse;
import com.charity_hub.shared.auth.RefreshTokenPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

@RestController
public class RefreshTokenController {
    private final RefreshTokenHandler refreshTokenHandler;

    public RefreshTokenController(RefreshTokenHandler refreshTokenHandler) {
        this.refreshTokenHandler = refreshTokenHandler;
    }

    @PostMapping("/v1/accounts/refresh-token")
    public ResponseEntity<RefreshTokenResponse> handle(
            @AuthenticationPrincipal RefreshTokenPayload refreshTokenPayload,
            HttpServletRequest request
    ) {
        // Extract the actual token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        String encodedRefreshToken = authHeader.substring("Bearer ".length());
        
        RefreshToken command = new RefreshToken(
                encodedRefreshToken,  // The actual JWT string
                UUID.fromString(refreshTokenPayload.getUuid()),
                refreshTokenPayload.getDeviceId()
        );


        String refreshToken = refreshTokenHandler.handle(command);
        return ResponseEntity.ok(new RefreshTokenResponse(refreshToken));
    }
}
