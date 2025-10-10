package com.charity_hub.cases.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;

public record PayContributionRequest(String PaymentProof) implements Request {
}
