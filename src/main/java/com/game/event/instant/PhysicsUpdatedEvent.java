package com.game.event.instant;

import com.game.collision.CollisionManager;
import com.game.event.InstantEvent;

public record PhysicsUpdatedEvent(CollisionManager collisionManager) implements InstantEvent { }
