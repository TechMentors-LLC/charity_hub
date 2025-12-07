package com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated;

import com.charity_hub.ledger.internal.application.contracts.IAccountGateway;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.application.contracts.IMembersNetworkRepo;
import com.charity_hub.ledger.internal.application.eventHandlers.loggers.AccountCreatedEventLogger;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedEventHandler {
    private final IMembersNetworkRepo memberShipRepo;
    private final IAccountGateway invitationGateway;
    private final INotificationService notificationService;
    private final AccountCreatedEventLogger logger;

    public AccountCreatedEventHandler(
            IMembersNetworkRepo memberShipRepo,
            IAccountGateway invitationGateway,
            INotificationService notificationService,
            AccountCreatedEventLogger logger
    ) {
        this.memberShipRepo = memberShipRepo;
        this.invitationGateway = invitationGateway;
        this.notificationService = notificationService;
        this.logger = logger;
    }

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
            Member newMember = Member.newMember(parentMember, account.id());
            memberShipRepo.save(newMember);
            logger.membershipCreated(account.id(), invitation.inviterId());
            
            notificationService.notifyNewConnectionAdded(newMember);
        } catch (Exception e) {
            logger.membershipCreationFailed(account.id(), invitation.inviterId(), e);
        }
    }
}