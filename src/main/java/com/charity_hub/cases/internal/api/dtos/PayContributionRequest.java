package com.charity_hub.cases.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;

public record PayRequest(String proofUrl) implements Request {
}
