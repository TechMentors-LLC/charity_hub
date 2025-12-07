package com.charity_hub.accounts.internal.core.commands.RegisterNotificationToken;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RegisterNotificationTokenHandler extends VoidCommandHandler<RegisterNotificationToken> {
    private final IAccountRepo accountRepo;

    public RegisterNotificationTokenHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public void handle(
            RegisterNotificationToken command
    ) {
                logger.info("Registering FCM token - UserId: {}, DeviceId: {}", 
                        command.userId(), command.deviceId());
                
                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(()-> {
                            logger.warn("Account not found for FCM registration - UserId: {}", command.userId());
                            return new NotFoundException("User with Id " + command.userId() + " not found");
                        });

                identity.registerFCMToken(
                        command.deviceId(),
                        command.fcmToken()
                );

                accountRepo.save(identity);
                logger.info("FCM token registered successfully - UserId: {}, DeviceId: {}", 
                        command.userId(), command.deviceId());
    }
}