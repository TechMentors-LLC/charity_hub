package com.charity_hub.shared.abstractions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class CommandHandler<TCommand extends Command, TResult> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public abstract TResult handle(TCommand command);
}