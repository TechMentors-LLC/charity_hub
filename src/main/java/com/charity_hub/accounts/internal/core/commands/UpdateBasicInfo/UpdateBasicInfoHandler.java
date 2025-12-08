package com.charity_hub.accounts.internal.core.commands.UpdateBasicInfo;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class UpdateBasicInfoHandler extends CommandHandler<UpdateBasicInfo,String> {
    private final IAccountRepo accountRepo;
    private final IJWTGenerator jwtGenerator;

    public UpdateBasicInfoHandler(IAccountRepo accountRepo, IJWTGenerator jwtGenerator) {
        this.accountRepo = accountRepo;
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    @Timed(value = "charity_hub.handler.update_basic_info", description = "Time taken by UpdateBasicInfoHandler")
    @Observed(name = "handler.update_basic_info", contextualName = "update-basic-info-handler")
    public String handle(
            UpdateBasicInfo command
    ) {
                logger.info("Updating basic info - UserId: {}, DeviceId: {}", 
                        command.userId(), command.deviceId());
                
                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(() -> {
                            logger.warn("Account not found for profile update - UserId: {}", command.userId());
                            return new NotFoundException("User with Id " + command.userId() + " not found");
                        });

                String accessToken = identity.updateBasicInfo(
                    command.deviceId(),
                    command.fullName(),
                    command.photoUrl(),
                    jwtGenerator
                );

                accountRepo.save(identity);
                logger.info("Basic info updated successfully - UserId: {}", command.userId());
                return accessToken;

    }
}