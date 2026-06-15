package com.game.collision;

import com.game.util.Vec2f;

import java.util.Optional;

public sealed interface Collider permits RectangleCollider {
    Collider moveToPosition(Vec2f position);
    boolean intersects(Collider other);
    Optional<Vec2f> minimumTranslationVector(Collider other);
    boolean isMobile();
}
