package com.charity_hub.cases.internal.application.commands.PayContribution;

import com.charity_hub.shared.abstractions.Command;

import java.util.UUID;

public record PayContribution(UUID contributionId, String paymentProof) implements Command {
}
