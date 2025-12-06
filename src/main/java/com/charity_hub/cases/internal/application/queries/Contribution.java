package com.charity_hub.cases.internal.application.queries;

import java.util.Date;
import java.util.UUID;

public record Contribution(
        String id,
        UUID contributorId,
        int caseCode,
        int amount,
        String status,
        Date contributionDate,
        String paymentProof
) {}