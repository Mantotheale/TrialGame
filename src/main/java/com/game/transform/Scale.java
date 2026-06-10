package com.game.transform;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record Scale(float x, float y, float z) {
    private final static Matrix4f WORKING_MEMORY = new Matrix4f();

    public Scale(float s) {
        this(s,s,s);
    }

    public Scale() {
        this(1);
    }

    public Scale(Vector3fc v) {
        this(v.x(), v.y(), v.z());
    }

    public Vector3f vec() {
        return new Vector3f(x,y,z);
    }

    public Matrix4f matrix() {
        return new Matrix4f().scaling(x, y, z);
    }

    public Vector3f transformMut(Vector3f vec) {
        return refreshWorkingMemory().transformPosition(vec);
    }

    private Matrix4f refreshWorkingMemory() {
        return WORKING_MEMORY.scaling(x, y, z);
    }
}
