package com.game.collision;

import com.game.util.Vec2f;

public record Collider(Vec2f center, float width, float height) {
    public Collider(Vec2f center, Vec2f dimensions) {
        this(center, dimensions.x(), dimensions.y());
    }
}
