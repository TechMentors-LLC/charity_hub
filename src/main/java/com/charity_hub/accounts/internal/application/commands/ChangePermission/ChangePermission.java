package com.charity_hub.accounts.internal.application.commands.ChangePermission;

import com.charity_hub.shared.abstractions.Command;

import java.util.UUID;

public record ChangePermission(UUID userId, String permission, boolean shouldAdd) implements Command {
}