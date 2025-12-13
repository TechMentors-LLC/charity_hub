package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionConfirmedLogger;
import com.charity_hub.ledger.internal.domain.model.*;
import com.charity_hub.shared.domain.IEventBus;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ContributionConfirmedHandler {
    private final IEventBus eventBus;
    private final ILedgerRepository ledgerRepository;
    private final IMembersNetworkRepo membersNetworkRepo;
    private final INotificationService notificationService;
    private final ContributionConfirmedLogger logger;

    public ContributionConfirmedHandler(
            IEventBus eventBus,
            ILedgerRepository ledgerRepository,
            IMembersNetworkRepo membersNetworkRepo,
            INotificationService notificationService,
            ContributionConfirmedLogger logger) {
        this.eventBus = eventBus;
        this.ledgerRepository = ledgerRepository;
        this.membersNetworkRepo = membersNetworkRepo;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionConfirmedDTO.class, this::handle);
    }

    @Observed(name = "ledger.event.contribution_confirmed", contextualName = "contribution-confirmed-handler")
    private void handle(ContributionConfirmedDTO contribution) {
        logger.processingEvent(contribution);

        try {
            // Find contributor's member record
            Member contributorMember = membersNetworkRepo.getById(contribution.contributorId());
            if (contributorMember == null) {
                logger.eventProcessingFailed(contribution.id(), contribution.contributorId(),
                        new IllegalStateException("Member not found"));
                return;
            }

            // Find contributor's ledger
            Ledger contributorLedger = ledgerRepository.findByMemberId(contributorMember.memberId());
            if (contributorLedger == null) {
                logger.eventProcessingFailed(contribution.id(), contribution.contributorId(),
                        new IllegalStateException("Ledger not found"));
                return;
            }

            Amount contributionAmount = Amount.forMember(contribution.amount());
            Amount networkAmount = Amount.forNetwork(contribution.amount());
            com.charity_hub.ledger.internal.domain.model.Service service = new com.charity_hub.ledger.internal.domain.model.Service(
                    ServiceType.CONTRIBUTION,
                    ServiceTransactionId.from(contribution.id()));

            logger.settlingChildObligation(contribution.contributorId(), contribution.amount());

            // Debit contributor's dueAmount and dueNetworkAmount (obligation settled)
            contributorLedger.debitDueAmount(contributionAmount, service);
            contributorLedger.debitNetworkAmount(networkAmount, service);
            ledgerRepository.save(contributorLedger);

            // Cascade debit dueNetworkAmount to all ancestors
            List<MemberId> ancestors = contributorMember.ancestors();
            if (!ancestors.isEmpty()) {
                List<UUID> ancestorUUIDs = ancestors.stream()
                        .map(MemberId::value)
                        .collect(java.util.stream.Collectors.toList());
                logger.cascadingToAncestors(contribution.contributorId(), ancestorUUIDs, contribution.amount());

                for (MemberId ancestorId : ancestors) {
                    Ledger ancestorLedger = ledgerRepository.findByMemberId(ancestorId);
                    if (ancestorLedger != null) {
                        ancestorLedger.debitNetworkAmount(networkAmount, service);
                        ledgerRepository.save(ancestorLedger);
                        logger.ancestorLedgerUpdated(ancestorId.value(), -contribution.amount());
                    }
                }
            }

            // Credit parent's dueAmount (parent now owes this up the tree)
            if (contributorMember.parent() != null) {
                Ledger parentLedger = ledgerRepository.findByMemberId(contributorMember.parent());
                if (parentLedger != null) {
                    parentLedger.creditDueAmount(contributionAmount, service);
                    ledgerRepository.save(parentLedger);
                    logger.parentObligationCreated(contributorMember.parent().value(), contribution.amount());
                }
            }

            logger.eventProcessedSuccessfully(contribution.id(), contribution.contributorId());
            notificationService.notifyContributionConfirmed(contribution);
            logger.notificationSent(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
        }
    }

}