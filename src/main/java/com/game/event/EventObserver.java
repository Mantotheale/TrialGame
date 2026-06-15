package com.game.event;

public interface EventObserver {
    void onEvent(EventDispatcher dispatcher, Event event);
}
