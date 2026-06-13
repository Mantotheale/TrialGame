package com.game.event;

import com.game.util.Observable;
import com.game.util.Observer;

import java.util.HashSet;
import java.util.Set;

public class EventDispatcher implements Observable<Event> {
    private final Set<Observer<Event>> observers = new HashSet<>();

    @Override
    public void addObserver(Observer<Event> observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer<Event> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Event value) {
        observers.forEach(observer -> observer.handle(value));
    }
}
