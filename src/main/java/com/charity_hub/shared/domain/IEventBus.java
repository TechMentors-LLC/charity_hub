package com.charity_hub.shared.domain;

public interface IEventBus {
    <T> void subscribe(Object owner, Class<T> event, EventCallback<T> callback);

    void unsubscribe(Object owner);

    <T> void push(T event);

    @FunctionalInterface
    public interface EventCallback<T> {
        void handle(T event);
    }
}
