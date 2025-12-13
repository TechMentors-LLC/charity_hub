package com.charity_hub.accounts.internal.application.commands.UpdateBasicInfo;

import com.charity_hub.shared.abstractions.Command;

import java.util.UUID;

public record UpdateBasicInfo(UUID userId, String deviceId, String fullName, String photoUrl) implements Command {
}