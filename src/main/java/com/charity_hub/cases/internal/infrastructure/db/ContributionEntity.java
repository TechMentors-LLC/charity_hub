package com.charity_hub.cases.internal.infrastructure.db;

public record ContributionEntity(String _id, String contributorId, int caseCode, int amount, int status,
                                 long contributionDate, String proofUrl) {
    public static final int STATUS_PLEDGED = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_CONFIRMED = 3;
}