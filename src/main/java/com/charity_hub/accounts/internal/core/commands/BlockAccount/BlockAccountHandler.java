package com.charity_hub.accounts.internal.core.commands.BlockAccount;

import com.charity_hub.accounts.internal.core.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BlockAccountHandler extends CommandHandler<BlockAccount, Void> {
    private final IAccountRepo accountRepo;

    public BlockAccountHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional
    public Void handle(BlockAccount command) {

        var identity = accountRepo.getById(UUID.fromString(command.userId()))
                .orElseThrow(() -> new NotFoundException("User with Id " + command.userId() + " not found"));

        if (command.isUnblock()) {
            identity.unBlock();
        } else {
            identity.block();
        }

        accountRepo.save(identity);

        return null;
    }
}