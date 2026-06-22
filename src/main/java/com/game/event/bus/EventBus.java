package com.game.event.bus;

import com.game.event.DeferredEvent;
import com.game.event.Event;
import com.game.event.InstantEvent;

import java.util.*;

public class EventBus {
    private final Map<Class<? extends InstantEvent>, EventDispatcher<? extends InstantEvent>> instantDispatchers;
    private final Map<Class<? extends DeferredEvent>, EventDispatcher<? extends DeferredEvent>> deferredDispatchers;
    private final Queue<DeferredEvent> deferredEventQueue;

    public EventBus() {
        this.instantDispatchers = new HashMap<>();
        this.deferredDispatchers = new HashMap<>();
        this.deferredEventQueue = new ArrayDeque<>();
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void addObserver(Class<T> eventType, EventObserver<T> observer) {
        if (InstantEvent.class.isAssignableFrom(eventType)) {
            var dispatcher = instantDispatchers.computeIfAbsent(
                    (Class<? extends InstantEvent>) eventType,
                    _ -> new EventDispatcher<>()
            );
            ((EventDispatcher<T>)dispatcher).addObserver(observer);
        } else if (DeferredEvent.class.isAssignableFrom(eventType)) {
            var dispatcher = deferredDispatchers.computeIfAbsent(
                    (Class<? extends DeferredEvent>) eventType,
                    _ -> new EventDispatcher<>()
            );
            ((EventDispatcher<T>) dispatcher).addObserver(observer);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Event> void removeObserver(Class<T> eventType, EventObserver<T> observer) {
        if (InstantEvent.class.isAssignableFrom(eventType)) {
            var dispatcher = instantDispatchers.get(eventType);
            if (dispatcher != null)
                ((EventDispatcher<T>)dispatcher).removeObserver(observer);
        } else if (DeferredEvent.class.isAssignableFrom(eventType)) {
            var dispatcher = deferredDispatchers.get(eventType);
            if (dispatcher != null)
                ((EventDispatcher<T>)dispatcher).removeObserver(observer);
        }
    }

    public void postEvent(Event event) {
        switch (event) {
            case InstantEvent instantEvent -> {
                var dispatcher = instantDispatchers.get(instantEvent.getClass());
                if (dispatcher != null)
                    notifyInstant(dispatcher, instantEvent);
            }
            case DeferredEvent deferredEvent -> deferredEventQueue.add(deferredEvent);
        }
    }

    public void dispatchDeferredEvents() {
        while (!deferredEventQueue.isEmpty()) {
            DeferredEvent event = deferredEventQueue.remove();
            var dispatcher = deferredDispatchers.get(event.getClass());
            if (dispatcher != null)
                notifyDeferred(dispatcher, event);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends InstantEvent> void notifyInstant(EventDispatcher<T> dispatcher, InstantEvent event) {
        dispatcher.notifyObservers(this, (T) event);
    }

    @SuppressWarnings("unchecked")
    private <T extends DeferredEvent> void notifyDeferred(EventDispatcher<T> dispatcher, DeferredEvent event) {
        dispatcher.notifyObservers(this, (T) event);
    }
}
