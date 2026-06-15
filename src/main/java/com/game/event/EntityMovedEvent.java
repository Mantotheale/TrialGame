package com.game.event;

import com.game.Entity;
import com.game.util.Vec2f;

public record EntityMovedEvent(Entity entity, Vec2f position) implements Event { }
