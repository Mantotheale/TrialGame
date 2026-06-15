package com.game.transform;

import com.game.util.Vec2f;

public record Scale2D(float x, float y) {
    public Scale2D(float s) {
        this(s, s);
    }

    public Vec2f toVec2f() {
        return new Vec2f(x, y);
    }

    public Scale2D compose(float sx, float sy) {
        return new Scale2D(x * sx, y * sy);
    }

    public Scale2D compose(float s) {
        return compose(s, s);
    }

    public Vec2f transform(Vec2f vec) {
        return new Vec2f(vec.x() * x, vec.y() * y);
    }

    public static final Scale2D UNIT = new Scale2D(1);
}
