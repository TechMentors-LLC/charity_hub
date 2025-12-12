package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionConfirmedLogger;
import com.charity_hub.ledger.internal.domain.model.*;
import com.charity_hub.shared.domain.IEventBus;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

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

    @Timed(value = "charity_hub.event.contribution_confirmed", description = "Time taken to handle ContributionConfirmed event")
    private void handle(ContributionConfirmedDTO contribution) {
        logger.processingEvent(contribution);

        try {
            // Get contributor's member info
            Member contributorMember = membersNetworkRepo.getById(contribution.contributorId());
            if (contributorMember == null) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(),
                        new IllegalStateException("Member not found"));
                return;
            }

            // Get contributor's ledger
            MemberId contributorId = new MemberId(contribution.contributorId());
            Ledger contributorLedger = ledgerRepository.findByMemberId(contributorId);
            if (contributorLedger == null) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(),
                        new IllegalStateException("Contributor ledger not found"));
                return;
            }

            // Get parent's ledger
            Ledger parentLedger = ledgerRepository.findByMemberId(contributorMember.parent());
            if (parentLedger == null) {
                logger.notificationFailed(contribution.id(), contribution.contributorId(),
                        new IllegalStateException("Parent ledger not found"));
                return;
            }

            // Update ledgers: contribution confirmed means member pledges to pay
            Amount contributionAmount = Amount.forMember(contribution.amount());
            com.charity_hub.ledger.internal.domain.model.Service service = new com.charity_hub.ledger.internal.domain.model.Service(
                    ServiceType.CONTRIBUTION,
                    ServiceTransactionId.from(contribution.id()));

            // Credit contributor's due amount (they now owe to parent)
            contributorLedger.creditDueAmount(contributionAmount, service);
            ledgerRepository.save(contributorLedger);

            // Credit parent's network due amount (parent expects from network)
            Amount networkAmount = Amount.forNetwork(contribution.amount());
            parentLedger.creditNetworkAmount(networkAmount, service);
            ledgerRepository.save(parentLedger);

            notificationService.notifyContributionConfirmed(contribution);
            logger.notificationSent(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
        }
    }
}