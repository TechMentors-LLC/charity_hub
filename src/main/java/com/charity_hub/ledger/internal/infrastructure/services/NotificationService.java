package com.charity_hub.ledger.internal.infrastructure.services;

import com.charity_hub.accounts.shared.AccountDTO;
import com.charity_hub.accounts.shared.IAccountsAPI;
import com.charity_hub.cases.shared.dtos.ContributionConfirmedDTO;
import com.charity_hub.cases.shared.dtos.ContributionPaidDTO;
import com.charity_hub.cases.shared.dtos.ContributionRemindedDTO;
import com.charity_hub.ledger.internal.domain.contracts.INotificationService;
import com.charity_hub.ledger.internal.domain.model.Member;
import com.charity_hub.ledger.internal.infrastructure.repositories.MembersNetworkRepo;
import com.charity_hub.notifications.NotificationApi;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.infrastructure.MessageService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component("ledgerNotificationService")
public class NotificationService implements INotificationService {
    private final MembersNetworkRepo membersNetworkRepo;
    private final NotificationApi notificationApi;
    private final IAccountsAPI accountsAPI;
    private final ILogger logger;
    private final MessageService messageService;

    public NotificationService(
            MembersNetworkRepo membersNetworkRepo,
            NotificationApi notificationApi,
            IAccountsAPI accountsAPI,
            ILogger logger,
            MessageService messageService
    ) {
        this.membersNetworkRepo = membersNetworkRepo;
        this.notificationApi = notificationApi;
        this.accountsAPI = accountsAPI;
        this.logger = logger;
        this.messageService = messageService;
    }

    @Override
    public CompletableFuture<Void> notifyContributionPaid(ContributionPaidDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            var parentAccount = getConnection(contribution.contributorId()).join();

            if (parentAccount == null) {
                logger.info("This is a root user, no parent account to notify");
                return;
            }

            List<String> accountTokens = parentAccount.devicesTokens();

            var contributor = accountsAPI.getById(contribution.contributorId()).join();
            if (contributor == null) {
                logger.info("Contributor not found when trying to notify collector");
                return;
            }

            notificationApi.notifyDevices(
                    accountTokens,
                    messageService.getMessage("notification.contribution.pending.title"),
                    messageService.getMessage("notification.contribution.pending.body", contributor.fullName())
            );
        });
    }

    @Override
    public CompletableFuture<Void> notifyContributionConfirmed(ContributionConfirmedDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            var parentAccount = getConnection(contribution.contributorId()).join();

            if (parentAccount == null) {
                logger.info("This is a root user, no parent account to notify");
                return;
            }

            List<String> accountTokens = parentAccount.devicesTokens();

            var contributor = getConnection(contribution.contributorId()).join();
            if (contributor == null) {
                logger.info("Contributor not found when trying to notify collector");
                return;
            }

            notificationApi.notifyDevices(
                    accountTokens,
                    messageService.getMessage("notification.contribution.confirmed.title"),
                    messageService.getMessage("notification.contribution.confirmed.body", contributor.fullName())
            );
        });
    }

    @Override
    public CompletableFuture<Void> notifyContributorToPay(ContributionRemindedDTO contribution) {
        return CompletableFuture.runAsync(() -> {
            var contributor = accountsAPI.getById(contribution.contributorId()).join();
            if (contributor == null) {
                logger.info("Contributor not found when trying to notify collector");
                return;
            }

            List<String> accountTokens = contributor.devicesTokens();

            notificationApi.notifyDevices(
                    accountTokens,
                    messageService.getMessage("notification.contribution.reminder.title"),
                    messageService.getMessage("notification.contribution.reminder.body")
            );
        });
    }

    @Override
    public CompletableFuture<Void> notifyNewConnectionAdded(Member member) {
        return CompletableFuture.runAsync(() -> {
            var parentAccount = accountsAPI.getById(member.parent().value()).join();
            if (parentAccount == null) {
                logger.info("Parent account not found when trying to notify new connection");
                return;
            }

            List<String> accountTokens = parentAccount.devicesTokens();

            var invited = accountsAPI.getById(member.memberId().value()).join();
            if (invited == null) {
                logger.info("Invited member not found when trying to notify parent account");
                return;
            }

            notificationApi.notifyDevices(
                    accountTokens,
                    messageService.getMessage("notification.connection.added.title", invited.fullName()),
                    messageService.getMessage("notification.connection.added.body")
            );
        });
    }

    private CompletableFuture<AccountDTO> getConnection(UUID userId) {
        return membersNetworkRepo.getById(userId)
                .thenCompose(member -> {
                    if (member == null) return null;
                    return accountsAPI.getById(member.parent().value());
                });
    }
}