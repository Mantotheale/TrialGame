package com.game.collision;

import com.game.util.Vec2f;

import java.util.Optional;

public sealed interface Collider permits RectangleCollider {
    Collider moveToPosition(Vec2f position);
    boolean intersects(Collider other);
    Optional<CollisionAxes> collisionAxes(Collider other);
    Optional<Vec2f> minimumTranslationVector(Vec2f beforeCenter, Collider other);
    boolean isMobile();
    Vec2f center();
}
