package com.game.event.deferred;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;
import com.game.math.Vec2f;

public record EntityMovedEvent(EntityId entityId, Vec2f position) implements DeferredEvent { }
