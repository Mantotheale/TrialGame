package com.game.event;

import java.util.*;

public class EventDispatcher {
    private final Set<EventObserver> observers = new HashSet<>();
    private final Queue<Event> eventQueue = new ArrayDeque<>();

    public void addObserver(EventObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(EventObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Event value) {
        observers.forEach(observer -> observer.onEvent(value));
    }

    public void pushEvent(Event event) {
        eventQueue.add(event);
    }

    public void dispatchEvents() {
        while (!eventQueue.isEmpty())
            notifyObservers(eventQueue.remove());
    }
}
