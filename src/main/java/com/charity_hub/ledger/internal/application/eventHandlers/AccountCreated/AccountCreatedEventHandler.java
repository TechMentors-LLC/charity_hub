package com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated;

import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.application.contracts.ILedgerRepository;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.AccountCreatedEventLogger;
import com.charity_hub.ledger.internal.domain.model.MemberId;
import io.micrometer.core.annotation.Timed;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedEventHandler {
    private final IMembersNetworkRepo memberShipRepo;
    private final ILedgerRepository ledgerRepository;
    private final IAccountGateway invitationGateway;
    private final INotificationService notificationService;
    private final AccountCreatedEventLogger logger;

    public AccountCreatedEventHandler(
            IMembersNetworkRepo memberShipRepo,
            ILedgerRepository ledgerRepository,
            IAccountGateway invitationGateway,
            INotificationService notificationService,
            AccountCreatedEventLogger logger) {
        this.memberShipRepo = memberShipRepo;
        this.ledgerRepository = ledgerRepository;
        this.invitationGateway = invitationGateway;
        this.notificationService = notificationService;
        this.logger = logger;
    }

    @Timed(value = "charity_hub.event.account_created", description = "Time taken to handle AccountCreated event")
    public void accountCreatedHandler(AccountCreated account) {
        logger.processingAccount(account.id(), account.mobileNumber());

        var invitation = invitationGateway.getInvitationByMobileNumber(account.mobileNumber());

        if (invitation == null) {
            logger.invitationNotFound(account.id(), account.mobileNumber());
            return;
        }

        var parentMember = memberShipRepo.getById(invitation.inviterId());
        if (parentMember == null) {
            logger.parentMemberNotFound(account.id(), invitation.inviterId());
            return;
        }

        try {
            // Create member in network
            Member newMember = Member.newMember(parentMember, account.id());
            memberShipRepo.save(newMember);

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