package com.game.event;

import com.game.Entity;
import com.game.util.Vec2f;

public record CollisionEvent(Entity e1, Entity e2, Vec2f minimumTranslationVector) implements Event { }
