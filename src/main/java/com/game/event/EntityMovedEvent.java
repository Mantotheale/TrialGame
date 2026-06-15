package com.game.event;

import com.game.Entity;

public record EntityMovedEvent(Entity entity) implements Event { }
