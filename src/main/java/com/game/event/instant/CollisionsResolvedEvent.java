package com.game.event.instant;

import com.game.entity.EntityId;
import com.game.event.InstantEvent;
import com.game.math.Vec2f;

public record CollisionsResolvedEvent(EntityId entityId, Vec2f finalVelocity, Vec2f finalPosition) implements InstantEvent { }
