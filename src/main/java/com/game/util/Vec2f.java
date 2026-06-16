package com.game.util;

import org.joml.Math;

public record Vec2f(float x, float y) {
    public Vec2f add(Vec2f other) {
        return new Vec2f(x + other.x, y + other.y);
    }

    public Vec2f mul(float scalar) {
        return new Vec2f(x * scalar, y * scalar);
    }

    public Vec2f normalize() {
        float lengthSquared = x * x + y * y;
        float invLength = Math.invsqrt(lengthSquared);
        return new Vec2f(x * invLength, y * invLength);
    }

    public static final Vec2f ZERO = new Vec2f(0, 0);
    public static final Vec2f UP = new Vec2f(0, 1);
    public static final Vec2f RIGHT = new Vec2f(1, 0);
    public static final Vec2f DOWN = new Vec2f(0, -1);
    public static final Vec2f LEFT = new Vec2f(-1, 0);
}
