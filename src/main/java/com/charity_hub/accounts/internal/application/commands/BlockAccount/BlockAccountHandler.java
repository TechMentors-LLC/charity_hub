package com.charity_hub.accounts.internal.application.commands.BlockAccount;

import com.charity_hub.accounts.internal.application.contracts.IAccountRepo;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BlockAccountHandler extends VoidCommandHandler<BlockAccount> {
    private final IAccountRepo accountRepo;

    public BlockAccountHandler(IAccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Override
    @Transactional
    @Observed(name = "handler.block_account", contextualName = "block-account-handler")
    public void handle(BlockAccount command) {
        String action = command.isUnblock() ? "UNBLOCK" : "BLOCK";
        logger.info("Account {} requested - UserId: {}", action, command.userId());
        
        var identity = accountRepo.getById(UUID.fromString(command.userId()))
                .orElseThrow(() -> {
                    logger.warn("Account not found for {} - UserId: {}", action, command.userId());
                    return new NotFoundException("User with Id " + command.userId() + " not found");
                });

        if (command.isUnblock()) {
            identity.unBlock();
        } else {
            identity.block();
        }

        accountRepo.save(identity);
        logger.info("Account {} completed successfully - UserId: {}", action, command.userId());
    }
}