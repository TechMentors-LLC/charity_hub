package com.charity_hub.cases.internal.application.commands.ChangeContributionStatus;

import com.charity_hub.shared.abstractions.Command;

import java.util.UUID;

public record ChangeContributionStatus(UUID contributionId,String proofUrl, boolean isPay) implements Command {
}