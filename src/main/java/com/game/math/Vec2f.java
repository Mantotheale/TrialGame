package com.game.math;

public record Vec2f(float x, float y) {
    public static final Vec2f ZERO = new Vec2f(0, 0);
    public static final Vec2f UP = new Vec2f(0, 1);
    public static final Vec2f DOWN = new Vec2f(0, -1);
    public static final Vec2f RIGHT = new Vec2f(1, 0);
    public static final Vec2f LEFT = new Vec2f(-1, 0);

    public Vec2f add(Vec2f other) {
        return new Vec2f(this.x + other.x, this.y + other.y());
    }

    public Vec2f sub(Vec2f other) {
        return new Vec2f(this.x - other.x, this.y - other.y());
    }

    public Vec2f negate() {
        return new Vec2f(-x, -y);
    }

    public Vec2f mul(float s) {
        return new Vec2f(x * s, y * s);
    }

    public float dot(Vec2f other) {
        return this.x * other.x + this.y * other.y;
    }

    public float squaredLen() {
        return x * x + y * y;
    }

    public float len() {
        return (float) Math.sqrt(squaredLen());
    }

    public float squaredDistance(Vec2f other) {
        float dx = this.x - other.x;
        float dy = this.y - other.y;
        return dx * dx + dy * dy;
    }

    public Vec2f normalize() {
        if (this.equals(Vec2f.ZERO)) return Vec2f.ZERO;

        float invLen = 1 / len();
        return new Vec2f(x * invLen, y * invLen);
    }

    public Vec2f reject(Vec2f normal) {
        return this.sub(normal.mul(this.dot(normal)));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        return (obj instanceof Vec2f(float x2, float y2))
                && FloatUtils.eq(x, x2)
                && FloatUtils.eq(y, y2);
    }
}