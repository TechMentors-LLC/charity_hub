package com.charity_hub.shared.abstractions;

public interface VoidQueryHandler<TQuery extends Query> {
    void handle(TQuery query);
}
