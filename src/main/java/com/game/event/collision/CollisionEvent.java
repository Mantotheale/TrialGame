package com.game.event.collision;

import com.game.Entity;
import com.game.event.Event;
import com.game.util.Vec2f;

public record CollisionEvent(Entity e1, Entity e2, Vec2f minimumTranslationVector) implements Event { }