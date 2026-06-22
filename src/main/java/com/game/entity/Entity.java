package com.game.entity;

import com.game.event.bus.EventBus;

public interface Entity {
    EntityId id();
    void delete(EventBus bus);
}
