package com.game.event.deferred;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;
import com.game.math.Vec2f;

public record CollisionEvent(EntityId e1, EntityId e2, Vec2f collisionPoint) implements DeferredEvent { }
