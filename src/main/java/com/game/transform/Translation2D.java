package com.game.transform;

import com.game.math.Vec2f;

public record Translation2D(float x, float y) {
    public Translation2D(Vec2f t) {
        this(t.x(), t.y());
    }

    public Vec2f transform(Vec2f vec) {
        return new Vec2f(vec.x() + x, vec.y() + y);
    }

    public Vec2f toVec2f() {
        return new Vec2f(x, y);
    }

    public Translation2D compose(Translation2D other) {
        return new Translation2D(x + other.x, y + other.y);
    }

    public Translation2D compose(float x, float y) {
        return new Translation2D(this.x + x, this.y + y);
    }

    public static final Translation2D ORIGIN = new Translation2D(0, 0);
}
