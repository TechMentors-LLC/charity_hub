package com.charity_hub.cases.shared.dtos;

public record ContributionDTO(String id,
                              String contributorId,
                              int caseCode,
                              int amount,
                              int status,
                              long contributionDate,
                              String proofUrl) {
}