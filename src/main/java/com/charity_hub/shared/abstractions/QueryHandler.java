package com.charity_hub.shared.abstractions;

public interface QueryHandler<TQuery extends Query, TResult> {
    TResult handle(TQuery query);
}