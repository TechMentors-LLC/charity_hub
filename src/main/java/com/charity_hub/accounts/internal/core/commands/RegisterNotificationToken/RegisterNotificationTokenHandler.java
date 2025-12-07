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

                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(()-> new NotFoundException("User with Id " + command.userId() + " not found"));

                identity.registerFCMToken(
                        command.deviceId(),
                        command.fcmToken()
                );

                accountRepo.save(identity);

    }
}