package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionPaidLogger;
import com.charity_hub.ledger.internal.domain.model.*;
import com.charity_hub.shared.domain.IEventBus;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class ContributionPaidHandler {
    private final IEventBus eventBus;
    private final ILedgerRepository ledgerRepository;
    private final IMembersNetworkRepo membersNetworkRepo;
    private final INotificationService notificationService;
    private final ContributionPaidLogger logger;

    public ContributionPaidHandler(
            IEventBus eventBus,
            ILedgerRepository ledgerRepository,
            IMembersNetworkRepo membersNetworkRepo,
            INotificationService notificationService,
            ContributionPaidLogger logger) {
        this.eventBus = eventBus;
        this.ledgerRepository = ledgerRepository;
        this.membersNetworkRepo = membersNetworkRepo;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionPaidDTO.class, this::handle);
    }

    @Timed(value = "charity_hub.event.contribution_paid", description = "Time taken to handle ContributionPaid event")
    private void handle(ContributionPaidDTO contribution) {
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

            // Update ledgers: contribution paid means money flows up the tree
            Amount contributionAmount = Amount.forMember(contribution.amount());
            com.charity_hub.ledger.internal.domain.model.Service service = new com.charity_hub.ledger.internal.domain.model.Service(
                    ServiceType.CONTRIBUTION,
                    ServiceTransactionId.from(contribution.id()));

            // Debit contributor's due amount (they paid what they owed)
            contributorLedger.debitDueAmount(contributionAmount, service);
            ledgerRepository.save(contributorLedger);

            // Debit parent's network due amount (parent received expected money)
            Amount networkAmount = Amount.forNetwork(contribution.amount());
            parentLedger.debitNetworkAmount(networkAmount, service);

            // Credit parent's due amount (parent now owes to THEIR parent)
            parentLedger.creditDueAmount(contributionAmount, service);
            ledgerRepository.save(parentLedger);

            notificationService.notifyContributionPaid(contribution);
            logger.notificationSent(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.notificationFailed(contribution.id(), contribution.contributorId(), e);
        }
    }
}