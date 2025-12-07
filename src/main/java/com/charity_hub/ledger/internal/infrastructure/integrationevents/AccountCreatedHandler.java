package com.charity_hub.ledger.internal.infrastructure.integrationevents;

import com.charity_hub.accounts.shared.AccountEventDto;
import com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated.AccountCreated;
import com.charity_hub.ledger.internal.application.eventHandlers.AccountCreated.AccountCreatedEventHandler;
import com.charity_hub.shared.domain.IEventBus;
import com.charity_hub.shared.domain.ILogger;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedHandler {
    private final IEventBus eventBus;
    private final ILogger logger;
    private final AccountCreatedEventHandler accountCreatedEventHandler;

    public AccountCreatedHandler(
            IEventBus eventBus,
            ILogger logger,
            AccountCreatedEventHandler accountCreatedEventHandler) {
        this.eventBus = eventBus;
        this.logger = logger;
        this.accountCreatedEventHandler = accountCreatedEventHandler;
    }

    public void start() {
        eventBus.subscribe(this, AccountEventDto.AccountCreatedDTO.class, this::addConnection);
    }

    private void addConnection(AccountEventDto.AccountCreatedDTO account) {
        logger.info("AddConnection Service received AccountCreated event, add the connection this account {}", account.id());
        accountCreatedEventHandler.accountCreatedHandler(new AccountCreated(account.id(), account.mobileNumber()));
    }
}