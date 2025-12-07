package com.charity_hub.shared.domain;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventBus implements IEventBus {
    // CopyOnWriteArrayList is thread-safe and doesn't require synchronization for iteration.
    // This avoids virtual thread pinning when handlers perform blocking I/O.
    private final List<EventListener<?>> listeners = new CopyOnWriteArrayList<>();

    @Override
    public <T> void subscribe(Object owner, Class<T> event, EventCallback<T> callback) {
        EventListener<T> listener = new EventListener<>(owner, event, callback);
        listeners.add(listener);
    }

    @Override
    public void unsubscribe(Object owner) {
        listeners.removeIf(listener -> listener.owner().equals(owner));
    }

    @Override
    public <T> void push(T event) {
        // No synchronization needed - CopyOnWriteArrayList handles thread-safety
        // and virtual threads won't be pinned during handler execution
        listeners.forEach(listener -> {
            if (listener.event().isInstance(event)) {
                @SuppressWarnings("unchecked")
                EventListener<T> typedListener = (EventListener<T>) listener;
                typedListener.callback().handle(event);
            }
        });
    }
}

record EventListener<T>(Object owner, Class<T> event, IEventBus.EventCallback<T> callback) {
}


