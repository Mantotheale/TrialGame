package com.game.transform;

import com.game.util.Vec2f;

public record Scale2D(float x, float y) {
    public Scale2D(float s) {
        this(s, s);
    }

    public Vec2f transform(Vec2f vec) {
        return new Vec2f(vec.x() * x, vec.y() * y);
    }

    public static final Scale2D UNIT = new Scale2D(1);
}
