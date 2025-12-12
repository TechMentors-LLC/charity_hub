package com.charity_hub.ledger.internal.infrastructure.db;

import com.charity_hub.ledger.internal.domain.model.AmountType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document entity for Ledger aggregate.
 */
@Data
@Document(collection = "ledgers")
public class LedgerEntity {

    @Id
    private String id;
    private String memberId;
    private AmountEntity dueAmount;
    private AmountEntity dueNetworkAmount;
    private List<TransactionEntity> transactions;

    @Data
    public static class AmountEntity {
        private int value;
        private AmountType type;
    }

    @Data
    public static class TransactionEntity {
        private String memberId;
        private String type; // DEBIT or CREDIT
        private ServiceEntity service;
        private AmountEntity amount;
        private Instant timestamp;
    }

    @Data
    public static class ServiceEntity {
        private String serviceType; // CONTRIBUTION
        private String serviceTransactionId;
    }
}
