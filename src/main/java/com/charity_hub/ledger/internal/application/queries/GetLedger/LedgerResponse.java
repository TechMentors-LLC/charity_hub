package com.charity_hub.ledger.internal.application.queries.GetLedger;

import java.util.List;

public record LedgerResponse(
        List<Contribution> contributions,
        long dueAmount,
        long dueNetworkAmount) {
}
