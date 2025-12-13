package com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated;

import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.AccountCreatedEventLogger;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import com.charity_hub.shared.domain.ILogger;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedEventHandler {
    private final IMembersNetworkRepo memberShipRepo;
    private final ILedgerRepository ledgerRepository;
    private final IAccountGateway invitationGateway;
    private final INotificationService notificationService;
    private final AccountCreatedEventLogger logger;
    private final ILogger rawLogger;

    public AccountCreatedEventHandler(
            IMembersNetworkRepo memberShipRepo,
            ILedgerRepository ledgerRepository,
            IAccountGateway invitationGateway,
            INotificationService notificationService,
            AccountCreatedEventLogger logger,
            ILogger rawLogger) {
        this.memberShipRepo = memberShipRepo;
        this.ledgerRepository = ledgerRepository;
        this.invitationGateway = invitationGateway;
        this.notificationService = notificationService;
        this.logger = logger;
        this.rawLogger = rawLogger;
    }

    @Timed(value = "charity_hub.event.account_created", description = "Time taken to handle AccountCreated event")
    public void accountCreatedHandler(AccountCreated account) {
        logger.processingAccount(account.id(), account.mobileNumber());

        var invitation = invitationGateway.getInvitationByMobileNumber(account.mobileNumber());

        if (invitation == null) {
            // No invitation = root user, create member without parent
            logger.invitationNotFound(account.id(), account.mobileNumber());
            rawLogger.info("Creating root member for account {} (no invitation found)", account.id());

            try {
                // Create root member (no parent)
                Member rootMember = Member.newRootMember(account.id());
                memberShipRepo.save(rootMember);

                // Create ledger for root member with zero balances
                MemberId memberId = new MemberId(account.id());
                Ledger newLedger = Ledger.createNew(memberId);
                ledgerRepository.save(newLedger);

                rawLogger.info("Root member and ledger created for account {}", account.id());
            } catch (Exception e) {
                rawLogger.error("Failed to create root member for account {}", account.id(), e);
            }
            return;
        }

        var parentMember = memberShipRepo.getById(invitation.inviterId());
        if (parentMember == null) {
            logger.parentMemberNotFound(account.id(), invitation.inviterId());
            return;
        }

        try {
            // Create member in network with parent
            Member newMember = Member.newMember(parentMember, account.id());
            memberShipRepo.save(newMember);

            // Update parent with new child
            Member updatedParent = parentMember.addChild(new MemberId(account.id()));
            memberShipRepo.save(updatedParent);

            // Create ledger for new member with zero balances
            MemberId memberId = new MemberId(account.id());
            Ledger newLedger = Ledger.createNew(memberId);
            ledgerRepository.save(newLedger);

            logger.membershipCreated(account.id(), invitation.inviterId());

            notificationService.notifyNewConnectionAdded(newMember);
        } catch (Exception e) {
            logger.membershipCreationFailed(account.id(), invitation.inviterId(), e);
        }
    }
}