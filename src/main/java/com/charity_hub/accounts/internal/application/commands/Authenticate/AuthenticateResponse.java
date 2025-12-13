package com.charity_hub.accounts.internal.application.commands.Authenticate;

public record AuthenticateResponse(String accessToken, String refreshToken) {
}