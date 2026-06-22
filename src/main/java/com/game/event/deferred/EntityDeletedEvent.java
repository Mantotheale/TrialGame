package com.game.event.deferred;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;

public record EntityDeletedEvent(EntityId entityId) implements DeferredEvent { }
