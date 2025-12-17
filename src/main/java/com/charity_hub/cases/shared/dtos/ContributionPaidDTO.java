package com.charity_hub.cases.shared.dtos;

import java.util.UUID;

public record ContributionPaidDTO(
                UUID id,
                UUID contributorId,
                int amount) implements CaseEventDto {
}
