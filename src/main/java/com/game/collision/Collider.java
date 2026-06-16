package com.game.collision;

import com.game.util.Vec2f;

import java.util.Optional;

public sealed interface Collider permits RectangleCollider {
    float EPSILON = 1e-5f;

    Collider moveToPosition(Vec2f position);
    boolean intersects(Collider other);
    Optional<Vec2f> minimumTranslationVector(Collider other);
    boolean isMobile();
}
