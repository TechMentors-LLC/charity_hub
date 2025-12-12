package com.charity_hub.ledger.internal.infrastructure.db;

import com.charity_hub.ledger.internal.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper between Ledger domain model and LedgerEntity.
 */
@Component
public class LedgerMapper {

    public LedgerEntity toEntity(Ledger ledger) {
        LedgerEntity entity = new LedgerEntity();
        entity.setId(ledger.getId().value().toString());
        entity.setMemberId(ledger.getMemberId().value().toString());
        entity.setDueAmount(toAmountEntity(ledger.getDueAmount()));
        entity.setDueNetworkAmount(toAmountEntity(ledger.getDueNetworkAmount()));
        entity.setTransactions(
                ledger.getTransactions().stream()
                        .map(this::toTransactionEntity)
                        .collect(Collectors.toList()));
        return entity;
    }

    public Ledger toDomain(LedgerEntity entity) {
        return new Ledger(
                LedgerId.from(entity.getId()),
                new MemberId(java.util.UUID.fromString(entity.getMemberId())),
                toAmountDomain(entity.getDueAmount()),
                toAmountDomain(entity.getDueNetworkAmount()),
                entity.getTransactions().stream()
                        .map(this::toTransactionDomain)
                        .collect(Collectors.toList()));
    }

    private LedgerEntity.AmountEntity toAmountEntity(Amount amount) {
        LedgerEntity.AmountEntity entity = new LedgerEntity.AmountEntity();
        entity.setValue(amount.value());
        entity.setType(amount.type());
        return entity;
    }

    private Amount toAmountDomain(LedgerEntity.AmountEntity entity) {
        return new Amount(entity.getValue(), entity.getType());
    }

    private LedgerEntity.TransactionEntity toTransactionEntity(Transaction transaction) {
        LedgerEntity.TransactionEntity entity = new LedgerEntity.TransactionEntity();
        entity.setMemberId(transaction.getMemberId().value().toString());
        entity.setType(transaction.getType().name());
        entity.setService(toServiceEntity(transaction.getService()));
        entity.setAmount(toAmountEntity(transaction.getAmount()));
        entity.setTimestamp(transaction.getTimestamp());
        return entity;
    }

    private Transaction toTransactionDomain(LedgerEntity.TransactionEntity entity) {
        return new Transaction(
                new MemberId(java.util.UUID.fromString(entity.getMemberId())),
                TransactionType.valueOf(entity.getType()),
                toServiceDomain(entity.getService()),
                toAmountDomain(entity.getAmount()),
                entity.getTimestamp());
    }

    private LedgerEntity.ServiceEntity toServiceEntity(Service service) {
        LedgerEntity.ServiceEntity entity = new LedgerEntity.ServiceEntity();
        entity.setServiceType(service.serviceType().name());
        entity.setServiceTransactionId(service.serviceTransactionId().value().toString());
        return entity;
    }

    private Service toServiceDomain(LedgerEntity.ServiceEntity entity) {
        return new Service(
                ServiceType.valueOf(entity.getServiceType()),
                ServiceTransactionId.from(entity.getServiceTransactionId()));
    }
}
