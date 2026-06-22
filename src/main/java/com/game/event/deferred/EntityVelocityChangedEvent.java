package com.game.event.deferred;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;
import com.game.math.Vec2f;

public record EntityVelocityChangedEvent(EntityId entityId, Vec2f velocity) implements DeferredEvent { }
