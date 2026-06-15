package com.game.event;

import com.game.Entity;
import com.game.collision.Collider;

public record CollisionEvent(Entity e1, Entity e2, Collider c1, Collider c2) implements Event { }
