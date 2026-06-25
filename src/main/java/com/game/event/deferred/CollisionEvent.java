package com.game.event.deferred;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;

public record CollisionEvent(EntityId e1, EntityId e2) implements DeferredEvent { }
