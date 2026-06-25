package com.game;

import com.game.entity.EntityId;
import com.game.event.DeferredEvent;

public record BulletCreated(EntityId id) implements DeferredEvent { }
