package com.charity_hub.accounts.internal.core.commands.Authenticate;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.core.contracts.IInvitationRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.BusinessRuleException;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


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
    @Observed(name = "handler.authenticate", contextualName = "authenticate-handler")
    public AuthenticateResponse handle(Authenticate command) {

        logger.info("Authentication attempt - DeviceId: {}, DeviceType: {}", command.deviceId(), command.deviceType());

        String mobileNumber = authProvider.getVerifiedMobileNumber(command.idToken());

        logger.debug("Mobile number verified: {}", mobileNumber);

        Account account = existingAccountOrNewAccount(mobileNumber, command);

        logger.debug("Account resolved for authentication - MobileNumber: {}", mobileNumber);
        var tokens = account.authenticate(
                command.deviceId(),
                command.deviceType(),
                jwtGenerator
        );

        accountRepo.save(account);
        logger.info("Authentication successful - MobileNumber: {}, DeviceId: {}", mobileNumber, command.deviceId());

        return new AuthenticateResponse(tokens.first, tokens.second);

    }

    private Account existingAccountOrNewAccount(String mobileNumber, Authenticate request) {

        return accountRepo.getByMobileNumber(mobileNumber)
                .map(account -> {
                    logger.debug("Existing account found - MobileNumber: {}", mobileNumber);
                    return account;
                })
                .orElseGet(() -> {
                    logger.info("Creating new account - MobileNumber: {}", mobileNumber);
                    return authenticateNewAccount(mobileNumber, request);
                });
    }

    private Account authenticateNewAccount(String mobileNumber, Authenticate request) {
        boolean isAdmin = accountRepo.isAdmin(mobileNumber);
        boolean hasNoInvitations = !invitationRepo.hasInvitation(mobileNumber);

        if (!isAdmin && hasNoInvitations) {
            logger.warn("Authentication rejected - Account not invited: {}", mobileNumber);
            throw new BusinessRuleException("Account not invited to use the App");
        }

        logger.info("New account created - MobileNumber: {}, IsAdmin: {}", mobileNumber, isAdmin);
        return Account.newAccount(
                mobileNumber,
                request.deviceId(),
                request.deviceType(),
                isAdmin
        );
    }
}