package com.charity_hub.ledger.internal.application.eventHandlers;

import com.charity_hub.cases.shared.dtos.ContributionMadeDTO;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.ContributionMadeLogger;
import com.charity_hub.ledger.internal.domain.model.*;
import com.charity_hub.shared.domain.IEventBus;
import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles ContributionMade events to update ledger state.
 * 
 * When a member makes a contribution:
 * 1. Credits the member's dueAmount (they now owe this to parent)
 * 2. Credits the member's dueNetworkAmount (for their network tracking)
 * 3. Cascades dueNetworkAmount credit to all ancestors up the tree
 */
@Service("ledgerContributionMadeHandler")
public class ContributionMadeHandler {
    private final IEventBus eventBus;
    private final ILedgerRepository ledgerRepository;
    private final IMembersNetworkRepo membersNetworkRepo;
    private final ContributionMadeLogger logger;

    public ContributionMadeHandler(
            IEventBus eventBus,
            ILedgerRepository ledgerRepository,
            IMembersNetworkRepo membersNetworkRepo,
            ContributionMadeLogger logger) {
        this.eventBus = eventBus;
        this.ledgerRepository = ledgerRepository;
        this.membersNetworkRepo = membersNetworkRepo;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        logger.handlerRegistered();
        eventBus.subscribe(this, ContributionMadeDTO.class, this::handle);
    }

    @Observed(name = "ledger.event.contribution_made", contextualName = "contribution-made-handler")
    private void handle(ContributionMadeDTO contribution) {
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

            // Credit contributor's dueAmount and dueNetworkAmount
            logger.creditingChildLedger(contribution.contributorId(), contribution.amount());
            contributorLedger.creditDueAmount(contributionAmount, service);
            contributorLedger.creditNetworkAmount(networkAmount, service);
            ledgerRepository.save(contributorLedger);

            // Cascade dueNetworkAmount to all ancestors
            List<MemberId> ancestors = contributorMember.ancestors();
            if (!ancestors.isEmpty()) {
                List<UUID> ancestorUUIDs = ancestors.stream()
                        .map(MemberId::value)
                        .collect(Collectors.toList());
                logger.cascadingToAncestors(contribution.contributorId(), ancestorUUIDs, contribution.amount());

                for (MemberId ancestorId : ancestors) {
                    Ledger ancestorLedger = ledgerRepository.findByMemberId(ancestorId);
                    if (ancestorLedger != null) {
                        ancestorLedger.creditNetworkAmount(networkAmount, service);
                        ledgerRepository.save(ancestorLedger);
                        logger.ancestorLedgerUpdated(ancestorId.value(), contribution.amount());
                    }
                }
            }

            logger.eventProcessedSuccessfully(contribution.id(), contribution.contributorId());
        } catch (Exception e) {
            logger.eventProcessingFailed(contribution.id(), contribution.contributorId(), e);
        }
    }
}
