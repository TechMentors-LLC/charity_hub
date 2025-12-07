package com.charity_hub.shared.abstractions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VoidCommandHandler<TCommand extends Command> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public abstract void handle(TCommand command);
}
