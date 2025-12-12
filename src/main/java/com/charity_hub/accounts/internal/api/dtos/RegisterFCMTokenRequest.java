package com.charity_hub.accounts.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;

public record RegisterFCMTokenRequest(String fcmToken) implements Request {
}
