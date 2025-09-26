package com.charity_hub.accounts.internal.core.commands.Authenticate;

public record AuthenticateResponse(String accessToken, String refreshToken, String welcomeMessage) {
}