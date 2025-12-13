package com.charity_hub.cases.internal.application.commands.ConfirmContribution;

import com.charity_hub.shared.abstractions.Command;

import java.util.UUID;

public record ConfirmContribution(UUID contributionId, UUID userId) implements Command {
}
