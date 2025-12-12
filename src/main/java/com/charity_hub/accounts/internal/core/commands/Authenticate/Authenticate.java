package com.charity_hub.accounts.internal.core.commands.Authenticate;

import com.charity_hub.shared.abstractions.Command;
import jakarta.validation.constraints.NotBlank;

public record Authenticate(
                @NotBlank(message = "ID token is required") String idToken,

                @NotBlank(message = "Device ID is required") String deviceId,

                @NotBlank(message = "Device type is required") String deviceType) implements Command {
}