package com.charity_hub.ledger.internal.infrastructure.services;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.accounts.shared.IAccountsAPI;
import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import com.charity_hub.notifications.shared.INotificationsAPI;
import com.charity_hub.shared.infrastructure.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("LedgerNotificationService")
public class NotificationService implements INotificationService {

    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final MembersNetworkRepo membersNetworkRepo;
    private final INotificationsAPI notificationApi;
    private final IAccountsAPI accountsAPI;
    private final MessageService messageService;

    public NotificationService(
            MembersNetworkRepo membersNetworkRepo,
            INotificationsAPI notificationApi,
            IAccountsAPI accountsAPI,
            MessageService messageService) {
        this.membersNetworkRepo = membersNetworkRepo;
        this.notificationApi = notificationApi;
        this.accountsAPI = accountsAPI;
        this.messageService = messageService;
    }

    @Override
    public void notifyContributionPaid(ContributionPaidDTO contribution) {
        var parentAccount = getConnection(contribution.contributorId());

        if (parentAccount == null) {
            logger.info("This is a root user, no parent account to notify");
            return;
        }

        List<String> accountTokens = parentAccount.devicesTokens();

        var contributor = accountsAPI.getById(contribution.contributorId());
        if (contributor == null) {
            logger.info("Contributor not found when trying to notify collector");
            return;
        }

        notificationApi.notifyDevices(
                accountTokens,
                messageService.getMessage("notification.contribution.pending.title"),
                messageService.getMessage("notification.contribution.pending.body", contributor.fullName()));
    }

    @Override
    public void notifyContributionConfirmed(ContributionConfirmedDTO contribution) {
        var parentAccount = getConnection(contribution.contributorId());

        if (parentAccount == null) {
            logger.info("This is a root user, no parent account to notify");
            return;
        }

        List<String> accountTokens = parentAccount.devicesTokens();

        var contributor = getConnection(contribution.contributorId());
        if (contributor == null) {
            logger.info("Contributor not found when trying to notify collector");
            return;
        }

        notificationApi.notifyDevices(
                accountTokens,
                messageService.getMessage("notification.contribution.confirmed.title"),
                messageService.getMessage("notification.contribution.confirmed.body", contributor.fullName()));
    }

    @Override
    public void notifyContributorToPay(ContributionRemindedDTO contribution) {
        var contributor = accountsAPI.getById(contribution.contributorId());
        if (contributor == null) {
            logger.info("Contributor not found when trying to notify collector");
            return;
        }

        List<String> accountTokens = contributor.devicesTokens();

        notificationApi.notifyDevices(
                accountTokens,
                messageService.getMessage("notification.contribution.reminder.title"),
                messageService.getMessage("notification.contribution.reminder.body"));
    }

    @Override
    public void notifyNewConnectionAdded(Member member) {
        var parentAccount = accountsAPI.getById(member.parent().value());
        if (parentAccount == null) {
            logger.info("Parent account not found when trying to notify new connection");
            return;
        }

        List<String> accountTokens = parentAccount.devicesTokens();

        var invited = accountsAPI.getById(member.memberId().value());
        if (invited == null) {
            logger.info("Invited member not found when trying to notify parent account");
            return;
        }

        notificationApi.notifyDevices(
                accountTokens,
                messageService.getMessage("notification.connection.added.title", invited.fullName()),
                messageService.getMessage("notification.connection.added.body"));
    }

    private AccountDTO getConnection(UUID userId) {
        var member = membersNetworkRepo.getById(userId);
        if (member == null)
            return null;
        return accountsAPI.getById(member.parent().value());
    }
}