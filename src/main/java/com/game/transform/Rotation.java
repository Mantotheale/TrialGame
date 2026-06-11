package com.game.transform;

import org.joml.*;

public record Rotation(float x, float y, float z, float w) {
    public final static Vector3fc WORLD_UP = new Vector3f(0, 1, 0);
    public final static Vector3fc WORLD_FRONT = new Vector3f(0, 0, -1);
    public final static Vector3fc WORLD_RIGHT = new Vector3f(1, 0, 0);

    public Rotation(float x, float y, float z, float w) {
        Quaternionf quaternion = new Quaternionf(x, y, z, w).normalize();
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        this.w = quaternion.w;
    }

    public Rotation(Quaternionfc q) {
        this(q.x(), q.y(), q.z(), q.w());
    }

    public Rotation() {
        this(0, 0, 0, 1);
    }

    public Vector3f direction() {
        return new Quaternionf(x, y, z, w).transform(WORLD_FRONT, new Vector3f());
    }

    public Matrix4f matrix() {
        return new Matrix4f().rotation(new Quaternionf(x, y, z, w));
    }

    public Quaternionfc quaternion() {
        return new Quaternionf(x, y, z, w);
    }

    public Vector3f transform(Vector3fc vec) {
        return new Quaternionf(x, y, z, w).transform(vec, new Vector3f());
    }

    public static Rotation fromDirection(Vector3fc dir) {
        return fromDirection(dir.x(), dir.y(), dir.z());
    }

    public static Rotation fromDirection(float x, float y, float z) {
        return new Rotation(new Quaternionf().rotationAxis(0, x,  y, z));
    }
}
