package com.charity_hub.accounts.internal.core.commands.RefreshToken;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.domain.ILogger;
import com.charity_hub.shared.exceptions.UnAuthorized;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenHandler extends CommandHandler<RefreshToken, String> {
    private final IAccountRepo accountRepo;
    private final IJWTGenerator jwtGenerator;
    private final ILogger logger;

    public RefreshTokenHandler(IAccountRepo accountRepo, IJWTGenerator jwtGenerator, ILogger logger) {
        this.accountRepo = accountRepo;
        this.jwtGenerator = jwtGenerator;
        this.logger = logger;
    }

    @Override
    public String handle(RefreshToken command) {
            logger.info("RefreshTokenHandler: Processing command: {}", command);
            logger.info("RefreshTokenHandler: UserId: {}", command.userId());
            logger.info("RefreshTokenHandler: DeviceId: {}", command.deviceId());

            var account = accountRepo.getById(command.userId())
                    .orElseGet(()->{
                        logger.error("RefreshTokenHandler: Account not found for userId: {}", command.userId());
                        throw new UnAuthorized("Unauthorized access.");
                    });


            String accessToken = account.refreshAccessToken(
                    command.deviceId(),
                    command.encodedRefreshToken(),
                    jwtGenerator
            );

            accountRepo.save(account);
            return accessToken;
    }
}