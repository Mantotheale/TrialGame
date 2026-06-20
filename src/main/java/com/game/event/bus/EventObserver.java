package com.game.event.bus;

import com.game.event.Event;

public interface EventObserver<T extends Event> {
    void onEvent(EventBus bus, T event);
}
