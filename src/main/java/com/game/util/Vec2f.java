package com.game.util;

public record Vec2f(float x, float y) {
    public static final Vec2f ZERO = new Vec2f(0, 0);
}
