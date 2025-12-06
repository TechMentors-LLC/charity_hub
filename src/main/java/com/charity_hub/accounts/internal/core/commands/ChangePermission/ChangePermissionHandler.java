package com.charity_hub.accounts.internal.core.commands.ChangePermission;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ChangePermissionHandler extends CommandHandler<ChangePermission, Void> {
    private final IAccountRepo accountRepo;

    public ChangePermissionHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public Void handle(ChangePermission command) {

                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(()-> new NotFoundException("User with Id " + command.userId() + " not found"));

                if (command.shouldAdd()) {
                    identity.addPermission(command.permission());
                } else {
                    identity.removePermission(command.permission());
                }

                accountRepo.save(identity);
        return null;
    }
}