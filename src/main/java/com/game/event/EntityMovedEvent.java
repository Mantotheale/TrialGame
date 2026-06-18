package com.game.event;

import com.game.Entity;
import com.game.math.Vec2f;

public record EntityMovedEvent(Entity entity, Vec2f position) implements Event { }
