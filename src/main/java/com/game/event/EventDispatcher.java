package com.game.event;

import java.util.*;

public class EventDispatcher {
    private final Set<EventObserver> currentObservers;
    private final Queue<Event> eventQueue;
    private boolean isDispatchingEvents;
    private boolean isNotifyingObservers;
    private final Queue<PendingOperation> pendingOperations;

    public EventDispatcher() {
        this.currentObservers = new HashSet<>();
        this.eventQueue = new ArrayDeque<>();
        this.isDispatchingEvents = false;
        this.isNotifyingObservers = false;
        this.pendingOperations = new ArrayDeque<>();
    }

    public void addObserver(EventObserver observer) {
        if (isNotifyingObservers)
            pendingOperations.add(new PendingOperation.Add(observer));
        else
            currentObservers.add(observer);
    }

    public void removeObserver(EventObserver observer) {
        if (isNotifyingObservers)
            pendingOperations.add(new PendingOperation.Remove(observer));
        else
            currentObservers.remove(observer);
    }

    public void pushEvent(Event event) {
        eventQueue.add(event);
    }

    public void dispatchEvents() {
        if (isDispatchingEvents) throw new IllegalStateException("Cannot dispatch events while another dispatch is ongoing");

        isDispatchingEvents = true;
        while (!eventQueue.isEmpty())
            notifyObservers(eventQueue.remove());
        isDispatchingEvents = false;
    }

    private void notifyObservers(Event value) {
        isNotifyingObservers = true;
        currentObservers.forEach(observer -> observer.onEvent(this, value));
        isNotifyingObservers = false;

        while (!pendingOperations.isEmpty())
            switch (pendingOperations.remove()) {
                case PendingOperation.Add(EventObserver observer) -> currentObservers.add(observer);
                case PendingOperation.Remove(EventObserver observer) -> currentObservers.remove(observer);
            }
    }

    private sealed interface PendingOperation {
        record Add(EventObserver observer) implements PendingOperation { }
        record Remove(EventObserver observer) implements PendingOperation { }
    }
}
