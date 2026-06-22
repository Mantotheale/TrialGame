package com.game.event.instant;

import com.game.entity.EntityId;
import com.game.event.InstantEvent;
import com.game.math.Vec2f;

public record CollisionResolvedEvent(EntityId entityId, Vec2f finalVelocity) implements InstantEvent { }
