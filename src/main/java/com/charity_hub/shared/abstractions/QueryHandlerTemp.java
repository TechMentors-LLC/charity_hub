package com.charity_hub.shared.abstractions;

import java.util.concurrent.CompletableFuture;

public interface QueryHandlerTemp<TQuery extends Query, TResult> {
    TResult handle(TQuery query);
}
