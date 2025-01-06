package com.charity_hub.accounts.internal.core.commands.BlockAccount;

import com.charity_hub.shared.abstractions.Command;

public record BlockAccount(String userId, boolean isUnblock) implements Command {
}