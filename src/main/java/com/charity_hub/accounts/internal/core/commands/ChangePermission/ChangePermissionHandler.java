package com.charity_hub.accounts.internal.core.commands.ChangePermission;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChangePermissionHandler extends VoidCommandHandler<ChangePermission> {
    private final IAccountRepo accountRepo;

    public ChangePermissionHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    public void handle(ChangePermission command) {
                var identity = accountRepo.getById(command.userId())
                        .orElseThrow(()-> new NotFoundException("User with Id " + command.userId() + " not found"));

                if (command.shouldAdd()) {
                    identity.addPermission(command.permission());
                } else {
                    identity.removePermission(command.permission());
                }

                accountRepo.save(identity);

    }
}