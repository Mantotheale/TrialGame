package com.game.collision;

import com.game.util.Vec2f;

import java.util.Optional;

public sealed interface Collider permits RectangleCollider {
    Collider moveToPosition(Vec2f position);
    boolean intersects(Collider other);
    float intersectionArea(Collider other);
    Optional<Vec2f> minimumTranslationVector(Collider before, Collider other);
    boolean isMobile();
}
