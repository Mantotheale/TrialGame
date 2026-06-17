package com.game.event.collision;

import com.game.Entity;
import com.game.event.Event;
import com.game.util.Vec2f;

public record CollisionEvent(Entity entity, Entity other, Vec2f mtv) implements Event { }