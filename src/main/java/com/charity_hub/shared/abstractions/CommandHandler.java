package com.charity_hub.shared.abstractions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public abstract class CommandHandler<TCommand extends Command, TResult> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    public abstract CompletableFuture<TResult> handle(TCommand command);
}
