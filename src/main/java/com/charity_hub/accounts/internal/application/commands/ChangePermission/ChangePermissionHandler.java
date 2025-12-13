package com.charity_hub.accounts.internal.application.commands.ChangePermission;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePermissionHandler extends VoidCommandHandler<ChangePermission> {
    private final IAccountRepo accountRepo;

    public ChangePermissionHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional
    @Observed(name = "handler.change_permission", contextualName = "change-permission-handler")
    public void handle(ChangePermission command) {
        String action = command.shouldAdd() ? "ADD" : "REMOVE";
        logger.info("Permission change requested - UserId: {}, Permission: {}, Action: {}",
                command.userId(), command.permission(), action);

        var identity = accountRepo.getById(command.userId())
                .orElseThrow(() -> {
                    logger.warn("Account not found for permission change - UserId: {}", command.userId());
                    return new NotFoundException("User with Id " + command.userId() + " not found");
                });

        if (command.shouldAdd()) {
            identity.addPermission(command.permission());
        } else {
            identity.removePermission(command.permission());
        }

        accountRepo.save(identity);
        logger.info("Permission change completed - UserId: {}, Permission: {}, Action: {}",
                command.userId(), command.permission(), action);
    }
}