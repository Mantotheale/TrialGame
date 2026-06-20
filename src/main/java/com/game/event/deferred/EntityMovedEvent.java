package com.game.event.deferred;

import com.game.Entity;
import com.game.event.DeferredEvent;
import com.game.math.Vec2f;

public record EntityMovedEvent(Entity entity, Vec2f position) implements DeferredEvent { }
