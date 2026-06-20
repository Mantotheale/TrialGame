package com.game.event.bus;

import com.game.event.Event;

import java.util.*;

class EventDispatcher<T extends Event> {
    private final Set<EventObserver<T>> observers;
    private final Queue<PendingOperation<T>> pendingOperations;
    private int notificationDepth;

    public EventDispatcher() {
        observers = new HashSet<>();
        pendingOperations = new ArrayDeque<>();
        notificationDepth = 0;
    }

    public void addObserver(EventObserver<T> observer) {
        if (notificationDepth == 0)
            observers.add(observer);
        else
            pendingOperations.add(new PendingOperation.Add<>(observer));
    }

    public void removeObserver(EventObserver<T> observer) {
        if (notificationDepth == 0)
            observers.remove(observer);
        else
            pendingOperations.add(new PendingOperation.Remove<>(observer));
    }

    public void notifyObservers(EventBus bus, T event) {
        notificationDepth++;
        for (EventObserver<T> o: observers)
            o.onEvent(bus, event);
        notificationDepth--;

        if (notificationDepth == 0)
            while (!pendingOperations.isEmpty())
                switch (pendingOperations.remove()) {
                    case PendingOperation.Add(var observer) -> observers.add(observer);
                    case PendingOperation.Remove(var observer) -> observers.remove(observer);
                }
    }
}
