package com.game.event.bus;

import com.game.event.DeferredEvent;
import com.game.event.InstantEvent;

import java.util.*;

public class EventBus {
    private final EventDispatcher<InstantEvent> instantEventDispatcher;
    private final EventDispatcher<DeferredEvent> deferredEventDispatcher;
    private final Queue<DeferredEvent> deferredEventQueue;

    public EventBus() {
        this.instantEventDispatcher = new EventDispatcher<>();
        this.deferredEventDispatcher = new EventDispatcher<>();
        this.deferredEventQueue = new ArrayDeque<>();
    }

    public void addInstantObserver(EventObserver<InstantEvent> observer) {
        instantEventDispatcher.addObserver(observer);
    }

    public void addDeferredObserver(EventObserver<DeferredEvent> observer) {
        deferredEventDispatcher.addObserver(observer);
    }

    public void removeInstantObserver(EventObserver<InstantEvent> observer) {
        instantEventDispatcher.removeObserver(observer);
    }

    public void removeDeferredObserver(EventObserver<DeferredEvent> observer) {
        deferredEventDispatcher.removeObserver(observer);
    }

    public void postInstantEvent(InstantEvent event) {
        instantEventDispatcher.notifyObservers(this, event);
    }

    public void postDeferredEvent(DeferredEvent event) {
        deferredEventQueue.add(event);
    }

    public void dispatchDeferredEvents() {
        while (!deferredEventQueue.isEmpty())
            deferredEventDispatcher.notifyObservers(this, deferredEventQueue.remove());
    }
}
