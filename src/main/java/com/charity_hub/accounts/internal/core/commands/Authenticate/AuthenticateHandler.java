package com.charity_hub.accounts.internal.core.commands.Authenticate;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.core.model.account.AccountId;
import com.charity_hub.accounts.internal.core.model.account.Connection;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
public class AuthenticateHandler extends CommandHandler<Authenticate, AuthenticateResponse> {
    private final IAccountRepo accountRepo;
    private final IInvitationRepo invitationRepo;
    private final IAuthProvider authProvider;
    private final IJWTGenerator jwtGenerator;

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public AuthenticateHandler(
            IAccountRepo accountRepo,
            IInvitationRepo invitationRepo,
            IAuthProvider authProvider,
            IJWTGenerator jwtGenerator
    ) {
        this.accountRepo = accountRepo;
        this.invitationRepo = invitationRepo;
        this.authProvider = authProvider;
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public CompletableFuture<AuthenticateResponse> handle(Authenticate command) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Handling authentication for idToken: {}", command.idToken());

                String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken()).join();

                logger.info("check for account: {}", command);

                Account account = existingAccountOrNewAccount(mobileNumber, command);

                logger.info(" finish check for account: {}", command.idToken());
                var tokens = account.authenticate(
                        command.deviceId(),
                        command.deviceType(),
                        jwtGenerator
                );

                accountRepo.save(account);
                logger.info("Authentication successful for account: {}", account.getMobileNumber());

                return new AuthenticateResponse(tokens.first, tokens.second);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Account existingAccountOrNewAccount(String mobileNumber, Authenticate request) {
        logger.info("get the account by mobile number: {}" , mobileNumber);
        Account existingAccount = accountRepo.getByMobileNumber(mobileNumber).join();
        if (existingAccount != null) {
            return existingAccount;
        }
        return authenticateNewAccount(mobileNumber, request);
    }

    private Account authenticateNewAccount(String mobileNumber, Authenticate request) {
        boolean isAdmin = accountRepo.isAdmin(mobileNumber).join();
        boolean hasNoInvitations = !invitationRepo.hasInvitation(mobileNumber).join();

        if (!isAdmin && hasNoInvitations) {
            logger.warn("Account not invited: {}", mobileNumber);
            throw new BusinessRuleException("Account not invited to use the App");
        }

        AccountId newAccountId = AccountId.generate();

        Connection connection = new Connection(null, new ArrayList<>());

        var invitation = invitationRepo.get(mobileNumber).join();

        if (invitation != null && invitation.inviterId() != null) {
        // Set parent for the new account
        connection.setParent(new AccountId(invitation.inviterId()));

        // Add this account as a child to the inviter
        Account inviter = accountRepo.getById(invitation.inviterId()).join();
        if (inviter != null) {
            inviter.getConnections().addChild(newAccountId);
            accountRepo.save(inviter);
        }
    }

        return Account.newAccount(
                newAccountId,
                mobileNumber,
                request.deviceId(),
                request.deviceType(),
                isAdmin,
                connection
        );
    }
}