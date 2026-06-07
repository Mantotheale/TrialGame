package com.game.transform;

import org.joml.*;

public record Rotation(float x, float y, float z, float w) {
    public final static Vector3fc WORLD_UP = new Vector3f(0, 1, 0);
    public final static Vector3fc WORLD_FRONT = new Vector3f(0, 0, -1);
    public final static Vector3fc WORLD_RIGHT = new Vector3f(1, 0, 0);

    private final static Quaternionf WORKING_MEMORY = new Quaternionf();

    public Rotation(float x, float y, float z, float w) {
        WORKING_MEMORY.set(x, y, z, w).normalize();
        this.x = WORKING_MEMORY.x;
        this.y = WORKING_MEMORY.y;
        this.z = WORKING_MEMORY.z;
        this.w = WORKING_MEMORY.w;
    }

    public Rotation(Quaternionf q) {
        this(q.x, q.y, q.z, q.w);
    }

    public Rotation() {
        this(0, 0, 0, 1);
    }

    public Vector3f direction() {
        return refreshWorkingMemory().transform(WORLD_FRONT, new Vector3f());
    }

    public Matrix4f matrix() {
        return new Matrix4f().rotation(refreshWorkingMemory());
    }

    public static Rotation fromDirection(Vector3fc dir) {
        return fromDirection(dir.x(), dir.y(), dir.z());
    }

    public static Rotation fromDirection(float x, float y, float z) {
        return new Rotation(WORKING_MEMORY.rotationAxis(0, x,  y, z));
    }

    private Quaternionf refreshWorkingMemory() {
        return WORKING_MEMORY.set(x, y, z, w);
    }
}
