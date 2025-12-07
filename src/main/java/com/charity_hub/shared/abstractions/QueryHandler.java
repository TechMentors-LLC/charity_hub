package com.charity_hub.shared.abstractions;

import java.util.concurrent.CompletableFuture;

public interface QueryHandler<TQuery extends Query, TResult> {
    CompletableFuture<TResult> handle(TQuery query);
}
