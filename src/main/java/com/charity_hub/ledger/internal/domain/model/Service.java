package com.charity_hub.ledger.internal.domain.model;

/**
 * Value object representing the service context of a transaction.
 * Links transactions to their source service (e.g., Cases module) with type and
 * transaction ID.
 */
public record Service(ServiceType serviceType, ServiceTransactionId serviceTransactionId) {

    public Service {
        if (serviceType == null) {
            throw new IllegalArgumentException("ServiceType cannot be null");
        }
        if (serviceTransactionId == null) {
            throw new IllegalArgumentException("ServiceTransactionId cannot be null");
        }
    }

    public static Service forContribution(ServiceTransactionId contributionId) {
        return new Service(ServiceType.CONTRIBUTION, contributionId);
    }
}
