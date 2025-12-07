package com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateBasicInfoHandler extends CommandHandler<UpdateBasicInfo,String> {
    private final IAccountRepo accountRepo;
    private final IJWTGenerator jwtGenerator;

    public UpdateBasicInfoHandler(IAccountRepo accountRepo, IJWTGenerator jwtGenerator) {
        this.accountRepo = accountRepo;
        this.jwtGenerator = jwtGenerator;
    }

    public String handle(
            UpdateBasicInfo command
    ) {
                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(() -> new NotFoundException("User with Id " + command.userId() + " not found"));

                String accessToken = identity.updateBasicInfo(
                    command.deviceId(),
                    command.fullName(),
                    command.photoUrl(),
                    jwtGenerator
                );

                accountRepo.save(identity);
                return accessToken;

    }
}