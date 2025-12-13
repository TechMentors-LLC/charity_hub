package com.charity_hub.accounts.internal.application.queries;

import java.util.List;

public record GetConnectionResponse(List<Account> connections) {
}